package org.valkyrienskies.core.impl.shadow;

import javax.inject.Provider;

public final class ey {
    public static <T> ew<T> a(final Provider<T> provider) {
        ev.a(provider);
        return new ew<T>() {
            @Override
            public T get() {
                return provider.get();
            }
        };
    }

    private ey() {
    }
}
