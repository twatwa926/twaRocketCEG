package com.example.examplemod.rocket;

import java.util.Locale;

public class RocketFlightProgram {
    private final double targetOrbitAltitude;
    private final double turnStartAltitude;
    private final double turnEndAltitude;
    private final double maxThrust;
    private final String rawScript;

    private RocketFlightProgram(double targetOrbitAltitude, double turnStartAltitude, double turnEndAltitude,
                                double maxThrust, String rawScript) {
        this.targetOrbitAltitude = targetOrbitAltitude;
        this.turnStartAltitude = turnStartAltitude;
        this.turnEndAltitude = turnEndAltitude;
        this.maxThrust = maxThrust;
        this.rawScript = rawScript;
    }

    public static RocketFlightProgram defaultProgram() {
        return new RocketFlightProgram(2000.0, 200.0, 1200.0, 1.0,
                "target_orbit=2000\nturn_start=200\nturn_end=1200\nmax_thrust=1.0");
    }

    public static RocketFlightProgram parse(String script) {
        if (script == null || script.isBlank()) {
            return defaultProgram();
        }
        double targetOrbit = 2000.0;
        double turnStart = 200.0;
        double turnEnd = 1200.0;
        double maxThrust = 1.0;

        String[] lines = script.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            String[] parts = trimmed.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].trim().toLowerCase(Locale.ROOT);
            String value = parts[1].trim();
            try {
                double number = Double.parseDouble(value);
                switch (key) {
                    case "target_orbit" -> targetOrbit = number;
                    case "turn_start" -> turnStart = number;
                    case "turn_end" -> turnEnd = number;
                    case "max_thrust" -> maxThrust = number;
                    default -> {
                    }
                }
            } catch (NumberFormatException ignore) {
            }
        }

        return new RocketFlightProgram(targetOrbit, turnStart, turnEnd, maxThrust, script);
    }

    public double getTargetOrbitAltitude() {
        return targetOrbitAltitude;
    }

    public double getTurnStartAltitude() {
        return turnStartAltitude;
    }

    public double getTurnEndAltitude() {
        return turnEndAltitude;
    }

    public double getMaxThrust() {
        return maxThrust;
    }

    public String getRawScript() {
        return rawScript;
    }
}
