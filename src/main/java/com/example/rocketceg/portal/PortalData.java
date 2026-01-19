package com.example.rocketceg.portal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

/** 😡 传送门数据 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 的传送门设计： * 1. 支持任意方向和大小的传送门 * 2. 支持空间变换（平移、旋转、缩放） * 3. 支持双向传送 * 4. 支持跨维度渲染 😡
     */
public class PortalData {
    
    // 😡 传送门位置和方向 😡
    private Vec3 position;
    private Quaternionf rotation;
    private float width;
    private float height;
    
    // 😡 源维度和目标维度 😡
    private ResourceKey<Level> fromDimension;
    private ResourceKey<Level> toDimension;
    
    // 😡 目标位置和旋转 😡
    private Vec3 targetPosition;
    private Quaternionf targetRotation;
    
    // 😡 空间变换参数 😡
    private Vec3 translation;
    private float scale;
    private boolean mirror;
    
    // 😡 传送门状态 😡
    private boolean isActive;
    private long creationTime;
    
    public PortalData(
        Vec3 position,
        Quaternionf rotation,
        float width,
        float height,
        ResourceKey<Level> fromDimension,
        ResourceKey<Level> toDimension,
        Vec3 targetPosition,
        Quaternionf targetRotation
    ) {
        this.position = position;
        this.rotation = new Quaternionf(rotation);
        this.width = width;
        this.height = height;
        this.fromDimension = fromDimension;
        this.toDimension = toDimension;
        this.targetPosition = targetPosition;
        this.targetRotation = new Quaternionf(targetRotation);
        
        // 😡 默认参数 😡
        this.translation = Vec3.ZERO;
        this.scale = 1.0f;
        this.mirror = false;
        this.isActive = true;
        this.creationTime = System.currentTimeMillis();
    }
    
    // 😡 Getters and Setters 😡
    public Vec3 getPosition() {
        return position;
    }
    
    public void setPosition(Vec3 position) {
        this.position = position;
    }
    
    public Quaternionf getRotation() {
        return rotation;
    }
    
    public void setRotation(Quaternionf rotation) {
        this.rotation = new Quaternionf(rotation);
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public ResourceKey<Level> getFromDimension() {
        return fromDimension;
    }
    
    public ResourceKey<Level> getToDimension() {
        return toDimension;
    }
    
    public Vec3 getTargetPosition() {
        return targetPosition;
    }
    
    public void setTargetPosition(Vec3 targetPosition) {
        this.targetPosition = targetPosition;
    }
    
    public Quaternionf getTargetRotation() {
        return targetRotation;
    }
    
    public void setTargetRotation(Quaternionf targetRotation) {
        this.targetRotation = new Quaternionf(targetRotation);
    }
    
    public Vec3 getTranslation() {
        return translation;
    }
    
    public void setTranslation(Vec3 translation) {
        this.translation = translation;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public boolean isMirror() {
        return mirror;
    }
    
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    /** 😡 检查点是否在传送门内 😡
     */
    public boolean containsPoint(Vec3 point) {
        // 😡 计算点相对于传送门的位置 😡
        Vec3 relativePos = point.subtract(position);
        
        // 😡 应用反向旋转 😡
        org.joml.Vector3f vec = new org.joml.Vector3f((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
        Quaternionf inverseRotation = new Quaternionf(rotation).conjugate();
        inverseRotation.transform(vec);
        
        // 😡 检查是否在传送门范围内 😡
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        
        return Math.abs(vec.x) <= halfWidth && Math.abs(vec.y) <= halfHeight && Math.abs(vec.z) < 0.1f;
    }
    
    /** 😡 获取传送门的法向量 😡
     */
    public Vec3 getNormal() {
        org.joml.Vector3f normal = new org.joml.Vector3f(0, 0, 1);
        rotation.transform(normal);
        return new Vec3(normal.x, normal.y, normal.z);
    }
    
    /** 😡 获取传送门的右向量 😡
     */
    public Vec3 getRight() {
        org.joml.Vector3f right = new org.joml.Vector3f(1, 0, 0);
        rotation.transform(right);
        return new Vec3(right.x, right.y, right.z);
    }
    
    /** 😡 获取传送门的上向量 😡
     */
    public Vec3 getUp() {
        org.joml.Vector3f up = new org.joml.Vector3f(0, 1, 0);
        rotation.transform(up);
        return new Vec3(up.x, up.y, up.z);
    }
}
