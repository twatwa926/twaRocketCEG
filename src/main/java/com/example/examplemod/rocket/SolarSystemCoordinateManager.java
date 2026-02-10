package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class SolarSystemCoordinateManager {

    private static final double R_REF = 1e6;

    private static final double K = 2000.0;

    private static final double R_MAX_REAL = 5e12;

    private static final Map<ResourceLocation, BodyData> BODIES = new HashMap<>();

    static {

        put("sun", 0, 0, 0, 696000000);
        put("mercury", 57.9e9, 0, 0, 2439700);

        put("minecraft", "overworld", 149.6e9, 0, 0, 6371000);
        put(ExampleMod.MODID, "rocket_build", 149.6e9, 0, 0, 6371000);
        put("venus", 108.2e9, 0, 0, 6051800);
        put("earth", 149.6e9, 0, 0, 6371000);
        put("earth_orbit", 149.6e9, 0, 0, 6371000 + 400000);
        put("moon", 149.6e9 + 384.4e6, 0, 0, 1737400);
        put("mars", 228e9, 0, 0, 3389500);
        put("ceres", 413.7e9, 0, 0, 469730);
        put("jupiter", 778.5e9, 0, 0, 69911000);
        put("saturn", 1434e9, 0, 0, 58232000);
        put("uranus", 2871e9, 0, 0, 25362000);
        put("neptune", 4495e9, 0, 0, 24622000);
        put("pluto", 5906e9, 0, 0, 1188300);
    }

    private static void put(String name, double x, double y, double z, double radius) {
        BODIES.put(new ResourceLocation(ExampleMod.MODID, name), new BodyData(x, y, z, radius));
    }

    private static void put(String namespace, String path, double x, double y, double z, double radius) {
        BODIES.put(new ResourceLocation(namespace, path), new BodyData(x, y, z, radius));
    }

    private SolarSystemCoordinateManager() {}

    public static double realToMc(double rReal) {
        if (rReal <= 0) return 0;
        rReal = Math.min(rReal, R_MAX_REAL);
        return K * Math.log(1.0 + rReal / R_REF);
    }

    public static double mcToReal(double rMc) {
        if (rMc <= 0) return 0;
        return R_REF * (Math.exp(rMc / K) - 1.0);
    }

    public static Vec3 localToSolar(Vec3 local, ResourceKey<Level> dimKey) {
        BodyData body = BODIES.get(dimKey.location());
        if (body == null) return local;
        return new Vec3(body.x + local.x, body.y + local.y, body.z + local.z);
    }

    public static Vec3 solarToLocal(Vec3 solar, ResourceKey<Level> dimKey) {
        BodyData body = BODIES.get(dimKey.location());
        if (body == null) return solar;
        return new Vec3(solar.x - body.x, solar.y - body.y, solar.z - body.z);
    }

    public static BodyData getBody(ResourceKey<Level> dimKey) {
        return BODIES.get(dimKey.location());
    }

    public static boolean hasBody(ResourceKey<Level> dimKey) {
        return BODIES.containsKey(dimKey.location());
    }

    public static Vec3 transformPosition(Vec3 localFrom, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (from.equals(to)) return localFrom;
        BodyData fromBody = BODIES.get(from.location());
        BodyData toBody = BODIES.get(to.location());
        if (fromBody == null || toBody == null) return localFrom;

        Vec3 solar = localToSolar(localFrom, from);
        Vec3 localTo = solarToLocal(solar, to);
        double dist = localTo.length();

        if (dist > 100_000) {
            double rMc = realToMc(dist);
            Vec3 dir = dist > 1 ? localTo.normalize() : new Vec3(0, 1, 0);
            localTo = dir.scale(rMc);
        }
        return localTo;
    }

    public static final class BodyData {
        public final double x, y, z, radius;

        public BodyData(double x, double y, double z, double radius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }
}
