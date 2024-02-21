package net.openhft.chronicle.testframework.internal;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class BootstrapUtils {

    private static final String CHRONICLE_ENTERPRISE = "software.chronicle";
    private static final String CHRONICLE_CORE = "net.openhft.chronicle.core";
    private static final Set<String> PROTECTED_PACKAGES = new HashSet<>(asList(
            ".internal.", ".impl."
    ));

    private final Set<String> excluded;

    public BootstrapUtils() {
        this.excluded = Collections.emptySet();
    }

    public BootstrapUtils(Set<String> excluded) {
        this.excluded = excluded;
    }

    /**
     * @return classes that are MANUALLY verified to indirectly use Core's Bootstrap class
     */
    protected Set<String> getExcluded() {
        return excluded;
    } 

    public static void main(String... args) {
        new BootstrapUtils().scanClasses();
    }

    public Set<String> scanClasses() {
        Set<JavaClass> allClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(CHRONICLE_ENTERPRISE, CHRONICLE_CORE)
                .stream().filter(cls -> cls.getModifiers().contains(JavaModifier.PUBLIC))
                .collect(Collectors.toSet());

        Set<JavaClass> coreClasses = allClasses.stream()
                .filter(cls -> cls.getName().contains(CHRONICLE_CORE))
                .collect(Collectors.toSet());

        Set<JavaClass> protectedClasses = allClasses.stream()
                .filter(cls -> cls.getName().contains(CHRONICLE_ENTERPRISE))
                .filter(BootstrapUtils::isProtected)
                .collect(Collectors.toSet());

        Set<String> coreNames = coreClasses.stream()
                .map(JavaClass::getName)
                .collect(Collectors.toSet());

        Set<JavaClass> visited = new HashSet<>();
        Set<JavaClass> candidates = protectedClasses.stream()
                .flatMap(cls -> allReferrers(cls, visited).stream())
                .filter(BootstrapUtils::notProtected)
                .filter(cls -> cls.getDirectDependenciesFromSelf().stream()
                        .map(Dependency::getTargetClass)
                        .noneMatch(c -> coreNames.contains(c.getName())))
                .collect(Collectors.toSet());
        Set<String> candidatesNames = candidates.stream()
                .map(JavaClass::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        candidatesNames.removeAll(getExcluded());
        if (!candidatesNames.isEmpty()) {
            System.out.printf("\nPOTENTIAL BOOTSTRAP ISSUES FOUND (%s):\n%s", candidatesNames.size(),
                            String.join("\n", candidatesNames));
        }
        return candidatesNames;
    }

    @NotNull
    private static Set<JavaClass> allReferrers(JavaClass cls, Set<JavaClass> visited) {
        if (visited.contains(cls))
            return visited;
        Set<JavaClass> referrers = cls.getCodeUnitCallsToSelf().stream()
                .map(JavaCodeUnitAccess::getOriginOwner)
                .filter(c -> !c.equals(cls))
                .filter(BootstrapUtils::notProtected)
                .collect(Collectors.toSet());
        visited.add(cls);
        for (JavaClass c : referrers) {
            visited.addAll(allReferrers(c, visited));
        }
        return visited;
    }

    private static boolean isProtected(JavaClass cls) {
        return PROTECTED_PACKAGES.stream().anyMatch(p -> cls.getName().contains(p));
    }

    private static boolean notProtected(JavaClass cls) {
        return PROTECTED_PACKAGES.stream().noneMatch(p -> cls.getName().contains(p));
    }

}
