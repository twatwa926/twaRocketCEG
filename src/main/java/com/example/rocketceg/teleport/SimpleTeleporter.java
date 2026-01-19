package com.example.rocketceg.teleport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

/** 😡 简单的传送器实现 * * 用于指定传送的目标位置和旋转 😡
     */
public class SimpleTeleporter implements ITeleporter {
    
    private final Vec3 targetPos;
    private final float yRot;
    private final float xRot;
    
    public SimpleTeleporter(Vec3 targetPos, float yRot, float xRot) {
        this.targetPos = targetPos;
        this.yRot = yRot;
        this.xRot = xRot;
    }
    
    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, 
                            float yaw, Function<Boolean, Entity> repositionEntity) {
        // 😡 使用默认的实体重定位逻辑 😡
        return repositionEntity.apply(false);
    }
    
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, 
                                  Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        // 😡 返回我们指定的传送信息 😡
        return new PortalInfo(targetPos, Vec3.ZERO, yRot, xRot);
    }
}