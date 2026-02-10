package org.valkyrienskies.core.impl.shadow;

public final class ev {
    public static <T> T a(final T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T a(final T reference, final String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    public static <T> T b(final T reference) {
        if (reference == null) {
            throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
        }
        return reference;
    }

    private static <T> T c(final T reference) {
        if (reference == null) {
            throw new NullPointerException("Cannot return null from a non-@Nullable component method");
        }
        return reference;
    }

    private static <T> T a(T reference, final String errorMessageTemplate, final Object errorMessageArg) {
        if (reference != null) {
            return reference;
        }
        if (!errorMessageTemplate.contains("%s")) {
            throw new IllegalArgumentException("errorMessageTemplate has no format specifiers");
        }
        if (errorMessageTemplate.indexOf("%s") != errorMessageTemplate.lastIndexOf("%s")) {
            throw new IllegalArgumentException("errorMessageTemplate has more than one format specifier");
        }
        reference = (T) String.valueOf((errorMessageArg instanceof Class)
                ? ((Class<?>) errorMessageArg).getCanonicalName()
                : errorMessageArg);
        throw new NullPointerException(errorMessageTemplate.replace("%s", String.valueOf(reference)));
    }

    public static <T> void a(final T requirement, final Class<T> clazz) {
        if (requirement == null) {
            throw new IllegalStateException(clazz.getCanonicalName() + " must be set");
        }
    }

    private ev() {
    }
}
