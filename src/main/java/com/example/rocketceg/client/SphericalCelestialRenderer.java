package com.example.rocketceg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 😡 球形天体渲染器 - 使用数学公式计算球面顶点 * 渲染真实的球形星球（像现实中看到的那样） 😡
     */
public class SphericalCelestialRenderer {
    
    /** 😡 渲染球形天体 😡
     */
    public static void renderSphere(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        Vector3f color,
        float radius,
        int segments,
        int rings
    ) {
        poseStack.pushPose();
        poseStack.translate(position.x, position.y, position.z);
        
        // 😡 使用 lines 模式渲染线框球体（最简单且稳定） 😡
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        
        // 😡 渲染经线 😡
        for (int seg = 0; seg < segments; seg++) {
            float theta = (float) seg / segments * Mth.TWO_PI;

            for (int ring = 0; ring < rings; ring++) {
                float phi1 = (float) ring / rings * Mth.PI;

                float phi2 = (float) (ring + 1) / rings * Mth.PI;

                // 😡 计算两个点的位置 😡
                Vector3f p1 = sphericalToCartesian(radius, theta, phi1);
                Vector3f p2 = sphericalToCartesian(radius, theta, phi2);
                
                // 😡 绘制线段 😡
                consumer.vertex(matrix, p1.x, p1.y, p1.z)
                    .color(color.x, color.y, color.z, 1.0f)
                    .normal(0, 1, 0)
                    .endVertex();
                consumer.vertex(matrix, p2.x, p2.y, p2.z)
                    .color(color.x, color.y, color.z, 1.0f)
                    .normal(0, 1, 0)
                    .endVertex();
            }
        }
        
        // 😡 渲染纬线 😡
        for (int ring = 1; ring < rings; ring++) {
            float phi = (float) ring / rings * Mth.PI;

            for (int seg = 0; seg < segments; seg++) {
                float theta1 = (float) seg / segments * Mth.TWO_PI;

                float theta2 = (float) (seg + 1) / segments * Mth.TWO_PI;

                // 😡 计算两个点的位置 😡
                Vector3f p1 = sphericalToCartesian(radius, theta1, phi);
                Vector3f p2 = sphericalToCartesian(radius, theta2, phi);
                
                // 😡 绘制线段 😡
                consumer.vertex(matrix, p1.x, p1.y, p1.z)
                    .color(color.x, color.y, color.z, 1.0f)
                    .normal(0, 1, 0)
                    .endVertex();
                consumer.vertex(matrix, p2.x, p2.y, p2.z)
                    .color(color.x, color.y, color.z, 1.0f)
                    .normal(0, 1, 0)
                    .endVertex();
            }
        }
        
        poseStack.popPose();
    }
    
    /** 😡 球面坐标转笛卡尔坐标 * @param r 半径 * @param theta 经度角 (0 到 2π) * @param phi 纬度角 (0 到 π) * @return 笛卡尔坐标 (x, y, z) 😡
     */
    private static Vector3f sphericalToCartesian(float r, float theta, float phi) {
        float x = r * Mth.sin(phi) * Mth.cos(theta);

        float y = r * Mth.cos(phi);

        float z = r * Mth.sin(phi) * Mth.sin(theta);

        return new Vector3f(x, y, z);
    }
    
    /** 😡 渲染带大气层的球形天体 😡
     */
    public static void renderSphereWithAtmosphere(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        Vector3f bodyColor,
        Vector3f atmosphereColor,
        float radius,
        int segments,
        int rings
    ) {
        // 😡 渲染主体 😡
        renderSphere(poseStack, bufferSource, position, bodyColor, radius, segments, rings);
        
        // 😡 渲染大气层（稍大一点） 😡
        renderSphere(poseStack, bufferSource, position, atmosphereColor, radius * 1.1f, segments / 2, rings / 2);

    }
}
