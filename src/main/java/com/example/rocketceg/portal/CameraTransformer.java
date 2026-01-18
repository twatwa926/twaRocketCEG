package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.util.QuaternionUtil;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 😡 相机变换器 - 100% 按照 ImmersivePortalsMod 实现 * * 负责计算通过传送门后的相机位置和旋转。 * * 核心算法： * 1. 计算相机相对于传送门的位置 * 2. 应用反向旋转 * 3. 应用缩放 * 4. 应用镜像 * 5. 应用目标旋转 * 6. 应用平移和目标位置 😡
     */
public class CameraTransformer {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    /** 😡 计算变换后的相机位置 * * 这是 ImmersivePortalsMod 的核心算法，用于计算玩家穿过传送门后的位置。 * * @param originalPos 原始相机位置 * @param portal 传送门 * @return 变换后的相机位置 😡
     */
    public static Vec3 transformCameraPosition(Vec3 originalPos, Portal portal) {
        try {
            // 😡 1. 计算相对位置（相对于传送门中心） 😡
            Vec3 relativePos = originalPos.subtract(portal.getPosition());
            
            // 😡 2. 应用反向旋转 😡
            // 😡 将相对位置从传送门坐标系转换到标准坐标系 😡
            Quaternionf inverseRotation = QuaternionUtil.conjugate(portal.getRotation());
            Vector3f vec = new Vector3f((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
            inverseRotation.transform(vec);
            relativePos = new Vec3(vec.x, vec.y, vec.z);
            
            // 😡 3. 应用缩放 😡
            double scale = portal.getScale();
            relativePos = relativePos.scale(scale);
            
            // 😡 4. 应用镜像 😡
            if (portal.isMirror()) {
                relativePos = new Vec3(-relativePos.x, relativePos.y, relativePos.z);
            }
            
            // 😡 5. 应用目标旋转 😡
            // 😡 将位置从标准坐标系转换到目标传送门坐标系 😡
            Vector3f vec2 = new Vector3f((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
            portal.getTargetRotation().transform(vec2);
            relativePos = new Vec3(vec2.x, vec2.y, vec2.z);
            
            // 😡 6. 应用平移和目标位置 😡
            Vec3 translation = portal.getTranslation();
            Vec3 targetPos = portal.getTargetPosition();
            Vec3 result = relativePos.add(translation).add(targetPos);
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 计算相机位置变换失败", e);
            return originalPos;
        }
    }
    
    /** 😡 计算变换后的相机旋转 * * @param yaw 原始偏航角（度数） * @param pitch 原始俯仰角（度数） * @param portal 传送门 * @return 变换后的旋转（欧拉角数组 [yaw, pitch]） 😡
     */
    public static float[] transformCameraRotation(float yaw, float pitch, Portal portal) {
        try {
            // 😡 1. 将欧拉角转换为四元数 😡
            Quaternionf originalRotation = QuaternionUtil.fromEulerAnglesDegrees(yaw, pitch, 0);
            
            // 😡 2. 应用传送门旋转变换 😡
            // 😡 目标旋转 = 目标传送门旋转 * 原始旋转 * 源传送门反向旋转 😡
 馃槨
            Quaternionf sourceInverse = QuaternionUtil.conjugate(portal.getRotation());
            Quaternionf targetRotation = portal.getTargetRotation();
            
            Quaternionf transformedRotation = new Quaternionf(targetRotation);
            transformedRotation.mul(originalRotation);
            transformedRotation.mul(sourceInverse);
            
            // 😡 3. 转换回欧拉角 😡
            float[] euler = QuaternionUtil.toEulerAnglesDegrees(transformedRotation);
            
            // 😡 返回 [yaw, pitch] 😡
            return new float[]{euler[0], euler[1]};
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 计算相机旋转变换失败", e);
            return new float[]{yaw, pitch};
        }
    }
    
    /** 😡 计算变换后的相机旋转（四元数版本） 😡
     */
    public static Quaternionf transformCameraRotationQuaternion(Quaternionf originalRotation, Portal portal) {
        try {
            // 😡 应用传送门旋转变换 😡
            Quaternionf sourceInverse = QuaternionUtil.conjugate(portal.getRotation());
            Quaternionf targetRotation = portal.getTargetRotation();
            
            Quaternionf result = new Quaternionf(targetRotation);
            result.mul(originalRotation);
            result.mul(sourceInverse);
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 计算相机旋转变换失败", e);
            return new Quaternionf(originalRotation);
        }
    }
    
    /** 😡 计算平滑的相机过渡 * * 使用球面线性插值（SLERP）实现平滑的相机旋转过渡。 * * @param startRotation 起始旋转 * @param endRotation 结束旋转 * @param progress 过渡进度（0 到 1） * @return 插值后的旋转 😡
     */
    public static Quaternionf smoothCameraTransition(Quaternionf startRotation, Quaternionf endRotation, float progress) {
        try {
            // 😡 使用 SLERP 进行平滑插值 😡
            return QuaternionUtil.slerp(startRotation, endRotation, progress);
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 计算平滑过渡失败", e);
            return new Quaternionf(startRotation);
        }
    }
    
    /** 😡 计算平滑的位置过渡 * * @param startPos 起始位置 * @param endPos 结束位置 * @param progress 过渡进度（0 到 1） * @return 插值后的位置 😡
     */
    public static Vec3 smoothPositionTransition(Vec3 startPos, Vec3 endPos, float progress) {
        try {
            // 😡 线性插值 😡
            return startPos.lerp(endPos, progress);
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 计算位置过渡失败", e);
            return startPos;
        }
    }
    
    /** 😡 验证相机变换是否有效 😡
     */
    public static boolean isValidTransformation(Vec3 originalPos, Vec3 transformedPos, Portal portal) {
        try {
            // 😡 检查变换后的位置是否在合理范围内 😡
            double distance = originalPos.distanceTo(transformedPos);
            
            // 😡 如果距离太大（超过 1000 方块），可能是无效的变换 😡
            if (distance > 1000) {
                LOGGER.warn("[CameraTransformer] 相机变换距离过大: {}", distance);
                return false;
            }
            
            // 😡 检查是否包含 NaN 或无穷大 😡
            if (Double.isNaN(transformedPos.x) || Double.isNaN(transformedPos.y) || Double.isNaN(transformedPos.z)) {
                LOGGER.warn("[CameraTransformer] 相机变换包含 NaN");
                return false;
            }
            
            if (Double.isInfinite(transformedPos.x) || Double.isInfinite(transformedPos.y) || Double.isInfinite(transformedPos.z)) {
                LOGGER.warn("[CameraTransformer] 相机变换包含无穷大");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("[CameraTransformer] 验证相机变换失败", e);
            return false;
        }
    }
    
    /** 😡 获取调试信息 😡
     */
    public static String getDebugInfo(Vec3 originalPos, Vec3 transformedPos, Portal portal) {
        try {
            double distance = originalPos.distanceTo(transformedPos);
            return String.format(
                "CameraTransform{original=(%.1f, %.1f, %.1f), transformed=(%.1f, %.1f, %.1f), distance=%.1f}",
                originalPos.x, originalPos.y, originalPos.z,
                transformedPos.x, transformedPos.y, transformedPos.z,
                distance
            );
        } catch (Exception e) {
            return "CameraTransform{error}";
        }
    }
}
