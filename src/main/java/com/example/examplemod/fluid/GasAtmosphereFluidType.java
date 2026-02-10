package com.example.examplemod.fluid;

import net.minecraftforge.fluids.FluidType;

public class GasAtmosphereFluidType extends FluidType {

    public GasAtmosphereFluidType() {
        super(FluidType.Properties.create()
                .density(-100)
                .viscosity(500)
                .temperature(280)
                .lightLevel(0)
                .canSwim(false)
                .canDrown(true)
                .canExtinguish(false)
                .canHydrate(false)
                .canConvertToSource(false)
                .supportsBoating(false)
                .fallDistanceModifier(0.5f)
                .motionScale(0.002)
                .canPushEntity(true));
    }
}
