package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RocketNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private RocketNetwork() {
    }

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, RocketControlInputPacket.class,
                RocketControlInputPacket::encode,
                RocketControlInputPacket::decode,
                RocketControlInputPacket::handle);
        CHANNEL.registerMessage(id++, RocketControlLinkPacket.class,
                RocketControlLinkPacket::encode,
                RocketControlLinkPacket::decode,
                RocketControlLinkPacket::handle);
        CHANNEL.registerMessage(id++, RocketProgramUpdatePacket.class,
                RocketProgramUpdatePacket::encode,
                RocketProgramUpdatePacket::decode,
                RocketProgramUpdatePacket::handle);
        CHANNEL.registerMessage(id++, RocketLaunchPacket.class,
                RocketLaunchPacket::encode,
                RocketLaunchPacket::decode,
                RocketLaunchPacket::handle);
        CHANNEL.registerMessage(id++, RocketShipStatePacket.class,
                RocketShipStatePacket::encode,
                RocketShipStatePacket::decode,
                RocketShipStatePacket::handle);
        CHANNEL.registerMessage(id++, RocketShipStructurePacket.class,
                RocketShipStructurePacket::encode,
                RocketShipStructurePacket::decode,
                RocketShipStructurePacket::handle);
        CHANNEL.registerMessage(id++, RocketShipBreakBlockPacket.class,
                RocketShipBreakBlockPacket::encode,
                RocketShipBreakBlockPacket::decode,
                RocketShipBreakBlockPacket::handle);
        CHANNEL.registerMessage(id++, SeamlessTeleportPacket.class,
                SeamlessTeleportPacket::encode,
                SeamlessTeleportPacket::decode,
                SeamlessTeleportPacket::handle);
    }
}
