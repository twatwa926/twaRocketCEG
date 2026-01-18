package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** 😡 空间变换系统 * * 参考 ImmersivePortalsMod 的空间变换理念： * 1. 支持平移（Translation）变换 * 2. 支持旋转（Rotation）变换 * 3. 支持缩放（Scale）变换 * 4. 支持镜像（Mirror）变换 * 5. 基于眼部位置的精确变换 * * 这是实现真正无缝维度切换的数学基础 😡
     */
public class SpatialTransformation {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 变换参数 😡
    private Vec3 translation;
    private Quaternionf rotation;
    private float scale;
    private boolean mirrorX;
    private boolean mirrorY;
    private boolean mirrorZ;
    
    // 😡 变换矩阵 😡
    private Matrix4f transformMatrix;
    private Matrix4f inverseMatrix;
    
    /** 😡 创建默认的空间变换（无变换） 😡
     */
    public SpatialTransformation() {
        this(Vec3.ZERO, new Quaternionf(), 1.0f, false, false, false);
    }
    
    /** 😡 创建完整的空间变换 😡
     */
    public SpatialTransformation(Vec3 translation, Quaternionf rotation, float scale, 
                               boolean mirrorX, boolean mirrorY, boolean mirrorZ) {
        this.translation = translation;
        this.rotation = new Quaternionf(rotation);
        this.scale = scale;
        this.mirrorX = mirrorX;
        this.mirrorY = mirrorY;
        this.mirrorZ = mirrorZ;
        
        updateTransformMatrix();
    }
    
    /** 😡 创建简单的平移变换 😡
     */
    public static SpatialTransformation translation(Vec3 offset) {
        return new SpatialTransformation(offset, new Quaternionf(), 1.0f, false, false, false);
    }
    
    /** 😡 创建简单的旋转变换 😡
     */
    public static SpatialTransformation rotation(Quaternionf rotation) {
        return new SpatialTransformation(Vec3.ZERO, rotation, 1.0f, false, false, false);
    }
    
    /** 😡 创建简单的缩放变换 😡
     */
    public static SpatialTransformation scale(float scale) {
        return new SpatialTransformation(Vec3.ZERO, new Quaternionf(), scale, false, false, false);
    }
    
    /** 😡 创建镜像变换 😡
     */
    public static SpatialTransformation mirror(boolean x, boolean y, boolean z) {
        return new SpatialTransformation(Vec3.ZERO, new Quaternionf(), 1.0f, x, y, z);
    }
    
    /** 😡 更新变换矩阵 😡
     */
    private void updateTransformMatrix() {
        transformMatrix = new Matrix4f();
        
        // 😡 1. 应用平移 😡
        transformMatrix.translate((float)translation.x, (float)translation.y, (float)translation.z);
        
        // 😡 2. 应用旋转 😡
        transformMatrix.rotate(rotation);
        
        // 😡 3. 应用缩放 😡
        if (scale != 1.0f) {
            transformMatrix.scale(scale);
        }
        
        // 😡 4. 应用镜像 😡
        float scaleX = mirrorX ? -1.0f : 1.0f;
        float scaleY = mirrorY ? -1.0f : 1.0f;
        float scaleZ = mirrorZ ? -1.0f : 1.0f;
        
        if (scaleX != 1.0f || scaleY != 1.0f || scaleZ != 1.0f) {
            transformMatrix.scale(scaleX, scaleY, scaleZ);
        }
        
        // 😡 计算逆矩阵 😡
        inverseMatrix = new Matrix4f(transformMatrix).invert();
    }
    
    /** 😡 变换位置向量 😡
     */
    public Vec3 transformPosition(Vec3 position) {
        Vector3f pos = new Vector3f((float)position.x, (float)position.y, (float)position.z);
        transformMatrix.transformPosition(pos);
        return new Vec3(pos.x, pos.y, pos.z);
    }
    
    /** 😡 逆变换位置向量 😡
     */
    public Vec3 inverseTransformPosition(Vec3 position) {
        Vector3f pos = new Vector3f((float)position.x, (float)position.y, (float)position.z);
        inverseMatrix.transformPosition(pos);
        return new Vec3(pos.x, pos.y, pos.z);
    }
    
    /** 😡 变换方向向量 😡
     */
    public Vec3 transformDirection(Vec3 direction) {
        Vector3f dir = new Vector3f((float)direction.x, (float)direction.y, (float)direction.z);
        transformMatrix.transformDirection(dir);
        return new Vec3(dir.x, dir.y, dir.z);
    }
    
    /** 😡 变换实体的眼部位置 - 参考 ImmersivePortalsMod 的核心算法 😡
     */
    public Vec3 transformEyePosition(Entity entity) {
        Vec3 eyePos = entity.getEyePosition();
        return transformPosition(eyePos);
    }
    
    /** 😡 从变换后的眼部位置计算实体位置 😡
     */
    public Vec3 calculateEntityPositionFromTransformedEye(Vec3 transformedEyePos, Entity entity) {
        // 😡 获取实体的眼部高度 😡
        double eyeHeight = entity.getEyeHeight();
        
        // 😡 计算实体位置（眼部位置减去眼部高度） 😡
        return new Vec3(transformedEyePos.x, transformedEyePos.y - eyeHeight, transformedEyePos.z);
    }
    
    /** 😡 变换玩家的视角 - 处理旋转变换对视角的影响 😡
     */
    public void transformPlayerView(ServerPlayer player) {
        if (rotation.equals(new Quaternionf())) {
            return; // 😡 没有旋转变换 😡
        }
        
        try {
            // 😡 获取当前视角 😡
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            
            // 😡 将欧拉角转换为四元数 😡
            Quaternionf currentRotation = new Quaternionf()
                .rotateY((float)Math.toRadians(yaw))
                .rotateX((float)Math.toRadians(pitch));
            
            // 😡 应用旋转变换 😡
            currentRotation.mul(rotation);
            
            // 😡 转换回欧拉角 😡
            Vector3f euler = currentRotation.getEulerAnglesYXZ(new Vector3f());
            float newYaw = (float)Math.toDegrees(euler.y);
            float newPitch = (float)Math.toDegrees(euler.x);
            
            // 😡 确保角度在有效范围内 😡
            newYaw = normalizeYaw(newYaw);
            newPitch = normalizePitch(newPitch);
            
            // 😡 设置新的视角 😡
            player.setYRot(newYaw);
            player.setXRot(newPitch);
            
            LOGGER.debug("[SpatialTransformation] 变换玩家视角: yaw {} -> {}, pitch {} -> {}", 
                        yaw, newYaw, pitch, newPitch);
            
        } catch (Exception e) {
            LOGGER.error("[SpatialTransformation] 变换玩家视角失败", e);
        }
    }
    
    /** 😡 标准化偏航角到 [-180, 180] 范围 😡
     */
    private float normalizeYaw(float yaw) {
        while (yaw > 180.0f) {
            yaw -= 360.0f;
        }
        while (yaw < -180.0f) {
            yaw += 360.0f;
        }
        return yaw;
    }
    
    /** 😡 标准化俯仰角到 [-90, 90] 范围 😡
     */
    private float normalizePitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }
    
    /** 😡 组合两个空间变换 😡
     */
    public SpatialTransformation compose(SpatialTransformation other) {
        // 😡 创建组合变换矩阵 😡
        Matrix4f combinedMatrix = new Matrix4f(this.transformMatrix);
        combinedMatrix.mul(other.transformMatrix);
        
        // 😡 从组合矩阵提取变换参数（简化实现） 😡
        Vector3f combinedTranslation = new Vector3f();
        Quaternionf combinedRotation = new Quaternionf();
        Vector3f combinedScale = new Vector3f();
        
        combinedMatrix.getTranslation(combinedTranslation);
        combinedMatrix.getUnnormalizedRotation(combinedRotation);
        combinedMatrix.getScale(combinedScale);
        
        // 😡 创建新的组合变换 😡
        return new SpatialTransformation(
            new Vec3(combinedTranslation.x, combinedTranslation.y, combinedTranslation.z),
            combinedRotation,
            combinedScale.x, // 😡 假设统一缩放 😡
            this.mirrorX || other.mirrorX,
            this.mirrorY || other.mirrorY,
            this.mirrorZ || other.mirrorZ
        );
    }
    
    /** 😡 获取逆变换 😡
     */
    public SpatialTransformation inverse() {
        // 😡 创建逆变换 😡
        Quaternionf inverseRotation = new Quaternionf(rotation).invert();
        Vec3 inverseTranslation = transformDirection(translation.scale(-1.0));
        float inverseScale = 1.0f / scale;
        
        return new SpatialTransformation(
            inverseTranslation,
            inverseRotation,
            inverseScale,
            mirrorX, mirrorY, mirrorZ // 😡 镜像变换的逆就是自身 😡
        );
    }
    
    /** 😡 检查是否是恒等变换（无变换） 😡
     */
    public boolean isIdentity() {
        return translation.equals(Vec3.ZERO) &&
               rotation.equals(new Quaternionf()) &&
               scale == 1.0f &&
               !mirrorX && !mirrorY && !mirrorZ;
    }
    
    /** 😡 创建用于太空到行星表面的变换 😡
     */
    public static SpatialTransformation createSpaceToPlanetTransform(Vec3 planetCenter, float planetRadius) {
        // 😡 从太空轨道传送到行星表面的变换 😡
        Vec3 surfaceOffset = new Vec3(0, planetRadius + 10, 0); // 😡 行星表面上方10方块 😡
        return translation(planetCenter.add(surfaceOffset));
    }
    
    /** 😡 创建用于行星表面到太空的变换 😡
     */
    public static SpatialTransformation createPlanetToSpaceTransform(Vec3 planetCenter, float orbitRadius) {
        // 😡 从行星表面传送到太空轨道的变换 😡
        Vec3 orbitOffset = new Vec3(0, orbitRadius, 0);
        return translation(planetCenter.add(orbitOffset));
    }
    
    // 😡 === Getter 和 Setter 方法 === 😡
    
    public Vec3 getTranslation() {
        return translation;
    }
    
    public void setTranslation(Vec3 translation) {
        this.translation = translation;
        updateTransformMatrix();
    }
    
    public Quaternionf getRotation() {
        return new Quaternionf(rotation);
    }
    
    public void setRotation(Quaternionf rotation) {
        this.rotation = new Quaternionf(rotation);
        updateTransformMatrix();
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
        updateTransformMatrix();
    }
    
    public boolean isMirrorX() {
        return mirrorX;
    }
    
    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
        updateTransformMatrix();
    }
    
    public boolean isMirrorY() {
        return mirrorY;
    }
    
    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
        updateTransformMatrix();
    }
    
    public boolean isMirrorZ() {
        return mirrorZ;
    }
    
    public void setMirrorZ(boolean mirrorZ) {
        this.mirrorZ = mirrorZ;
        updateTransformMatrix();
    }
    
    public Matrix4f getTransformMatrix() {
        return new Matrix4f(transformMatrix);
    }
    
    public Matrix4f getInverseMatrix() {
        return new Matrix4f(inverseMatrix);
    }
    
    @Override
    public String toString() {
        return String.format("SpatialTransformation{translation=%s, rotation=%s, scale=%.2f, mirror=[%s,%s,%s]}", 
                           translation, rotation, scale, mirrorX, mirrorY, mirrorZ);
    }
}