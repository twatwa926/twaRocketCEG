package com.example.rocketceg.network;

import com.example.rocketceg.dimension.seamless.SeamlessDimensionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 😡 无缝传送网络包 * * 用于在服务端和客户端之间同步无缝传送信息 * 这个包替代了标准的 respawn 包，避免触发加载屏幕 😡
     */
public class SeamlessTeleportPacket {
    
    private final ResourceKey<Level> targetDimension;
    private final Vec3 targetPosition;
    private final float yRot;
    private final float xRot;
    
    public SeamlessTeleportPacket(ResourceKey<Level> targetDimension, Vec3 targetPosition, 
                                float yRot, float xRot) {
        this.targetDimension = targetDimension;
        this.targetPosition = targetPosition;
        this.yRot = yRot;
        this.xRot = xRot;
    }
    
    /** 😡 从网络缓冲区读取包数据 😡
     */
    public static SeamlessTeleportPacket decode(FriendlyByteBuf buf) {
        ResourceLocation dimLocation = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimLocation);
        
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Vec3 position = new Vec3(x, y, z);
        
        float yRot = buf.readFloat();
        float xRot = buf.readFloat();
        
        return new SeamlessTeleportPacket(dimension, position, yRot, xRot);
    }
    
    /** 😡 将包数据写入网络缓冲区 😡
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(targetDimension.location());
        buf.writeDouble(targetPosition.x);
        buf.writeDouble(targetPosition.y);
        buf.writeDouble(targetPosition.z);
        buf.writeFloat(yRot);
        buf.writeFloat(xRot);
    }
    
    /** 😡 处理包（客户端） 😡
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                // 😡 在客户端处理无缝传送 😡
                SeamlessDimensionManager.getInstance().startSeamlessTeleport(
                    targetDimension, targetPosition
                );
                
                org.apache.logging.log4j.LogManager.getLogger(com.example.rocketceg.RocketCEGMod.MOD_ID).info(
                    "[RocketCEG] 客户端：收到无缝传送包 -> {} ({})", 
                    targetDimension.location(), targetPosition
                );
            });
        }
        
        context.setPacketHandled(true);
    }
    
    // 😡 Getters 😡
    public ResourceKey<Level> getTargetDimension() {
        return targetDimension;
    }
    
    public Vec3 getTargetPosition() {
        return targetPosition;
    }
    
    public float getYRot() {
        return yRot;
    }
    
    public float getXRot() {
        return xRot;
    }
}