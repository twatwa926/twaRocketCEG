package com.example.examplemod.client;

public final class RocketClientControlState {
    private static long controlledShipId = -1L;

    private RocketClientControlState() {
    }

    public static long getControlledShipId() {
        return controlledShipId;
    }

    public static void setControlledShipId(long shipId) {
        controlledShipId = shipId;
    }
}
