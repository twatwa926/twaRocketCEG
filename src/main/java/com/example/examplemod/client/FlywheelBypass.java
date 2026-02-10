package com.example.examplemod.client;

public final class FlywheelBypass {
    private FlywheelBypass() {}

    private static final ThreadLocal<Boolean> FORCE_SKIP_FLYWHEEL = ThreadLocal.withInitial(() -> false);

    public static void setForceSkipFlywheel(boolean value) {
        FORCE_SKIP_FLYWHEEL.set(value);
    }

    public static boolean getForceSkipFlywheel() {
        return Boolean.TRUE.equals(FORCE_SKIP_FLYWHEEL.get());
    }
}
