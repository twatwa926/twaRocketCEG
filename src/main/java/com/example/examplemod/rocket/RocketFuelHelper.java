package com.example.examplemod.rocket;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class RocketFuelHelper {
    private static final String GTCEU_ROCKET_FUEL_BUCKET = "gtceu:rocket_fuel_bucket";
    private static final String LAVA_BUCKET = "minecraft:lava_bucket";
    private static final double FUEL_PER_GTCEU = 120.0;
    private static final double FUEL_PER_LAVA = 60.0;
    private static final double MAX_FUEL_MASS = 2000.0;

    private RocketFuelHelper() {}

    public static boolean isRocketFuelItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return GTCEU_ROCKET_FUEL_BUCKET.equals(id) || LAVA_BUCKET.equals(id);
    }

    public static double getFuelPerBucket() {
        return FUEL_PER_GTCEU;
    }

    private static double getFuelForStack(ItemStack stack) {
        if (!isRocketFuelItem(stack)) return 0.0;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return LAVA_BUCKET.equals(id) ? FUEL_PER_LAVA : FUEL_PER_GTCEU;
    }

    public static double getMaxFuelMass() {
        return MAX_FUEL_MASS;
    }

    public static double getFuelAmount(ItemStack stack) {
        return getFuelForStack(stack);
    }

    public static double tryRefuel(double currentFuelMass, ItemStack stack) {
        if (!isRocketFuelItem(stack) || stack.isEmpty()) return 0.0;
        double remaining = MAX_FUEL_MASS - currentFuelMass;
        if (remaining <= 0.01) return 0.0;
        double perItem = getFuelForStack(stack);
        return Math.min(perItem, remaining);
    }
}
