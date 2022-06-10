package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Delegation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class DelegationBuilder<T, D> implements Delegation.Builder<T, D> {

    private final D delegate;
    @SuppressWarnings("unchecked")
    private Class<T> type = (Class<T>) Object.class;
    private Function<? super D, String> toStringFunction = Objects::toString;

    public DelegationBuilder(@NotNull final D delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public <N extends D> Delegation.Builder<N, D> as(@NotNull final Class<N> type) {
        requireNonNull(type);
        @SuppressWarnings("unchecked") final DelegationBuilder<N, D> newTypeBuilder = (DelegationBuilder<N, D>) this;
        newTypeBuilder.type = type;
        return newTypeBuilder;
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