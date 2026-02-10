package com.example.examplemod;

public enum RocketStage {
    STAGE_ONE(7600.0, 280.0, 120.0),
    STAGE_TWO(3800.0, 320.0, 60.0),
    STAGE_THREE(950.0, 350.0, 20.0);

    private final double wetMassTons;
    private final double ispSeconds;
    private final double dryMassTons;

    RocketStage(double wetMassTons, double ispSeconds, double dryMassTons) {
        this.wetMassTons = wetMassTons;
        this.ispSeconds = ispSeconds;
        this.dryMassTons = dryMassTons;
    }

    public double getWetMassTons() {
        return wetMassTons;
    }

    public double getIspSeconds() {
        return ispSeconds;
    }

    public double getDryMassTons() {
        return dryMassTons;
    }
}
