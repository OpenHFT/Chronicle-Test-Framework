package net.openhft.chronicle.testframework.internal;

import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class BootstrapUtils {

    private static final String CHRONICLE_ENTERPRISE = "software.chronicle";
    private static final String CHRONICLE_CORE = "net.openhft.chronicle.core";
    private static final Set<String> PROTECTED_PACKAGES = new HashSet<>(asList(
            ".internal.", ".impl."
    ));
    private final static Logger logger = LoggerFactory.getLogger(BootstrapUtils.class);

    private final Set<String> excluded;
    private final Function<ClassFileImporter, JavaClasses> classesSupplier;

    public BootstrapUtils() {
        this.excluded = Collections.emptySet();
        classesSupplier = cfi -> cfi.importPackages(CHRONICLE_ENTERPRISE, CHRONICLE_CORE);
    }

    public BootstrapUtils(Set<String> excluded) {
        this.excluded = excluded;
        classesSupplier = cfi -> cfi.importPackages(CHRONICLE_ENTERPRISE, CHRONICLE_CORE);
    }

    public BootstrapUtils(Function<ClassFileImporter, JavaClasses> classesSupplier, Set<String> excluded) {
        this.excluded = excluded;
        this.classesSupplier = classesSupplier;
    }

    /**
     * @return classes that are MANUALLY verified to indirectly use Core's Bootstrap class
     */
    protected Set<String> getExcluded() {
        return excluded;
    }

    public static void main(String... args) throws Exception {
        try (JarFile jar = new JarFile("")) {
            new BootstrapUtils(cfi -> cfi.importJar(jar), Collections.emptySet())
                    .scanClasses();
        }
    }

    public Set<String> scanClasses() {
        Set<JavaClass> allClasses = classesSupplier.apply(new ClassFileImporter()
                        .withImportOption(new ImportOption.DoNotIncludeTests()))
                .stream()
                .filter(cls -> cls.getModifiers().contains(JavaModifier.PUBLIC))
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
                .flatMap(cls -> allReferrers(cls, coreNames, visited).stream())
                .filter(BootstrapUtils::notProtected)
                .collect(Collectors.toSet());
        Set<String> candidatesNames = candidates.stream()
                .map(JavaClass::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        candidatesNames.removeAll(getExcluded());
        if (!candidatesNames.isEmpty()) {
            logger.error(format("\nPOTENTIAL BOOTSTRAP ISSUES FOUND (%s):\n%s\n", candidatesNames.size(),
                    String.join("\n", candidatesNames)));
        }
        return candidatesNames;
    }

    @NotNull
    private static Set<JavaClass> allReferrers(JavaClass cls, Set<String> coreNames, Set<JavaClass> visited) {
        if (visited.contains(cls))
            return visited;
        Set<JavaClass> referrers = cls.getCodeUnitCallsToSelf().stream()
                .map(JavaCodeUnitAccess::getOriginOwner)
                .filter(c -> !c.equals(cls))
                .filter(BootstrapUtils::notProtected)
                .filter(c -> c.getDirectDependenciesFromSelf().stream()
                        .map(Dependency::getTargetClass)
                        .noneMatch(cl -> coreNames.contains(cl.getName())))
                .collect(Collectors.toSet());
        visited.add(cls);
        for (JavaClass c : referrers) {
            visited.addAll(allReferrers(c, coreNames, visited));
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
