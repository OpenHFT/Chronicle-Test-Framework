package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Delegation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class DelegationBuilder<T, D> implements Delegation.Builder<T, D> {

    private final Class<T> type;
    private final D delegate;
    private Function<? super D, String> toStringFunction;

    public DelegationBuilder(@NotNull final Class<T> type,
                             @NotNull final D delegate) {
        this.type = requireNonNull(type);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Delegation.Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction) {
        this.toStringFunction = requireNonNull(toStringFunction);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T build() {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type}, (proxy1, method, args) -> {
                    if ("toString".equals(method.getName()) && args == null) {
                        return toStringFunction.apply(delegate);
                    }
                    return method.invoke(delegate, args);
                });
    }

}