package com.example.rocketceg.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 😡 四元数工具类 - 提供四元数相关的工具方法 * * 基于 JOML 库，提供便捷的四元数操作方法。 😡
     */
public class QuaternionUtil {
    
    /** 😡 从欧拉角创建四元数 * * @param yaw 偏航角（绕 Y 轴）- 单位：弧度 * @param pitch 俯仰角（绕 X 轴）- 单位：弧度 * @param roll 滚转角（绕 Z 轴）- 单位：弧度 * @return 四元数 😡
     */
    public static Quaternionf fromEulerAngles(float yaw, float pitch, float roll) {
        Quaternionf q = new Quaternionf();
        q.rotationYXZ(yaw, pitch, roll);
        return q;
    }
    
    /** 😡 从欧拉角创建四元数（度数） 😡
     */
    public static Quaternionf fromEulerAnglesDegrees(float yawDeg, float pitchDeg, float rollDeg) {
        return fromEulerAngles(
            (float)Math.toRadians(yawDeg),
            (float)Math.toRadians(pitchDeg),
            (float)Math.toRadians(rollDeg)
        );
    }
    
    /** 😡 将四元数转换为欧拉角 * * @param q 四元数 * @return 欧拉角数组 [yaw, pitch, roll]（单位：弧度） 😡
     */
    public static float[] toEulerAngles(Quaternionf q) {
        Vector3f euler = new Vector3f();
        q.getEulerAnglesYXZ(euler);
        return new float[]{euler.y, euler.x, euler.z};
    }
    
    /** 😡 将四元数转换为欧拉角（度数） 😡
     */
    public static float[] toEulerAnglesDegrees(Quaternionf q) {
        float[] radians = toEulerAngles(q);
        return new float[]{
            (float)Math.toDegrees(radians[0]),
            (float)Math.toDegrees(radians[1]),
            (float)Math.toDegrees(radians[2])
        };
    }
    
    /** 😡 应用四元数旋转到向量 😡
     */
    public static Vec3 rotateVector(Vec3 v, Quaternionf q) {
        Vector3f vec = new Vector3f((float)v.x, (float)v.y, (float)v.z);
        q.transform(vec);
        return new Vec3(vec.x, vec.y, vec.z);
    }
    
    /** 😡 获取四元数的共轭（逆旋转） 😡
     */
    public static Quaternionf conjugate(Quaternionf q) {
        return new Quaternionf(q).conjugate();
    }
    
    /** 😡 两个四元数的乘积 😡
     */
    public static Quaternionf multiply(Quaternionf q1, Quaternionf q2) {
        return new Quaternionf(q1).mul(q2);
    }
    
    /** 😡 四元数的逆 😡
     */
    public static Quaternionf inverse(Quaternionf q) {
        return new Quaternionf(q).invert();
    }
    
    /** 😡 四元数的归一化 😡
     */
    public static Quaternionf normalize(Quaternionf q) {
        return new Quaternionf(q).normalize();
    }
    
    /** 😡 两个四元数之间的球面线性插值 * * @param q1 起始四元数 * @param q2 结束四元数 * @param t 插值参数（0 到 1） * @return 插值结果 😡
     */
    public static Quaternionf slerp(Quaternionf q1, Quaternionf q2, float t) {
        Quaternionf result = new Quaternionf(q1);
        result.slerp(q2, t);
        return result;
    }
    
    /** 😡 获取四元数的模（长度） 😡
     */
    public static float magnitude(Quaternionf q) {
        return (float)Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
 馃槨
    }
    
    /** 😡 计算两个四元数之间的角度 * * @return 角度（单位：弧度） 😡
     */
    public static float angleBetween(Quaternionf q1, Quaternionf q2) {
        float dot = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;
 馃槨
        dot = Math.max(-1.0f, Math.min(1.0f, dot));
        return (float)Math.acos(dot) * 2.0f;
 馃槨
    }
    
    /** 😡 创建绕指定轴的旋转四元数 * * @param axis 旋转轴 * @param angle 旋转角度（单位：弧度） * @return 四元数 😡
     */
    public static Quaternionf fromAxisAngle(Vec3 axis, float angle) {
        Vector3f axisVec = new Vector3f((float)axis.x, (float)axis.y, (float)axis.z).normalize();
        Quaternionf q = new Quaternionf();
        q.fromAxisAngleRad(angle, axisVec.x, axisVec.y, axisVec.z);
        return q;
    }
    
    /** 😡 获取四元数表示的旋转轴 😡
     */
    public static Vec3 getAxis(Quaternionf q) {
        float sinHalfAngle = (float)Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
 馃槨
        
        if (sinHalfAngle < 0.0001f) {
            // 😡 旋转角度接近 0，返回默认轴 😡
            return new Vec3(0, 1, 0);
        }
        
        float invSinHalfAngle = 1.0f / sinHalfAngle;
        return new Vec3(
            q.x * invSinHalfAngle,
 馃槨
            q.y * invSinHalfAngle,
 馃槨
            q.z * invSinHalfAngle
 馃槨
        );
    }
    
    /** 😡 获取四元数表示的旋转角度 * * @return 旋转角度（单位：弧度） 😡
     */
    public static float getAngle(Quaternionf q) {
        return (float)(2.0 * Math.acos(Math.max(-1.0f, Math.min(1.0f, q.w))));
 馃槨
    }
    
    /** 😡 检查两个四元数是否相等（在容差范围内） 😡
     */
    public static boolean equals(Quaternionf q1, Quaternionf q2, float tolerance) {
        return Math.abs(q1.x - q2.x) < tolerance &&
               Math.abs(q1.y - q2.y) < tolerance &&
               Math.abs(q1.z - q2.z) < tolerance &&
               Math.abs(q1.w - q2.w) < tolerance;
    }
    
    /** 😡 获取四元数的字符串表示 😡
     */
    public static String toString(Quaternionf q) {
        float[] euler = toEulerAnglesDegrees(q);
        return String.format("Quaternion(yaw=%.1f°, pitch=%.1f°, roll=%.1f°)", 
            euler[0], euler[1], euler[2]);
    }
}
