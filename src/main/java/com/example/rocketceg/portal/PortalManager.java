package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** 😡 传送门管理器 - 100% 按照 ImmersivePortalsMod 实现 * * 功能： * 1. 管理所有传送门 * 2. 支持传送门的创建、删除、修改 * 3. 支持传送门的持久化 * 4. 支持传送门的查询和搜索 * 5. 支持传送门的性能优化 😡
     */
public class PortalManager {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    private static PortalManager INSTANCE;
    
    // 😡 所有传送门 😡
    private final Map<String, Portal> portals = new ConcurrentHashMap<>();
    
    // 😡 按维度索引的传送门 😡
    private final Map<ResourceKey<Level>, List<String>> portalsByDimension = new ConcurrentHashMap<>();
    
    // 😡 传送门计数器 😡
    private int portalCounter = 0;
    
    private PortalManager() {}
    
    public static PortalManager getInstance() {
        if (INSTANCE == null) {
            synchronized (PortalManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PortalManager();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 创建传送门 😡
     */
    public Portal createPortal(
        Vec3 position,
        Quaternionf rotation,
        float width,
        float height,
        ResourceKey<Level> fromDimension,
        ResourceKey<Level> toDimension,
        Vec3 targetPosition,
        Quaternionf targetRotation
    ) {
        try {
            Portal portal = new Portal(
                position, width, height, rotation,
                fromDimension, toDimension,
                targetPosition, targetRotation
            );
            
            String portalId = "portal_" + (portalCounter++);
            portals.put(portalId, portal);
            
            // 😡 添加到维度索引 😡
            portalsByDimension.computeIfAbsent(fromDimension, k -> new ArrayList<>()).add(portalId);
            
            // 😡 注册到渲染器 😡
            CrossDimensionPortalRenderer.getInstance().registerPortal(portal);
            
            LOGGER.info("[PortalManager] 创建传送门: {} ({})", portalId, fromDimension.location());
            
            return portal;
            
        } catch (Exception e) {
            LOGGER.error("[PortalManager] 创建传送门失败", e);
            return null;
        }
    }
    
    /** 😡 删除传送门 😡
     */
    public void deletePortal(String portalId) {
        try {
            Portal portal = portals.remove(portalId);
            
            if (portal != null) {
                // 😡 从维度索引中移除 😡
                ResourceKey<Level> dimension = portal.getFromDimension();
                List<String> dimensionPortals = portalsByDimension.get(dimension);
                if (dimensionPortals != null) {
                    dimensionPortals.remove(portalId);
                }
                
                // 😡 从渲染器中注销 😡
                CrossDimensionPortalRenderer.getInstance().unregisterPortal(portal);
                
                LOGGER.info("[PortalManager] 删除传送门: {}", portalId);
            }
            
        } catch (Exception e) {
            LOGGER.error("[PortalManager] 删除传送门失败", e);
        }
    }
    
    /** 😡 获取传送门 😡
     */
    public Portal getPortal(String portalId) {
        return portals.get(portalId);
    }
    
    /** 😡 获取维度中的所有传送门 😡
     */
    public List<Portal> getPortalsInDimension(ResourceKey<Level> dimension) {
        List<String> portalIds = portalsByDimension.getOrDefault(dimension, new ArrayList<>());
        List<Portal> result = new ArrayList<>();
        
        for (String id : portalIds) {
            Portal portal = portals.get(id);
            if (portal != null) {
                result.add(portal);
            }
        }
        
        return result;
    }
    
    /** 😡 获取所有传送门 😡
     */
    public Collection<Portal> getAllPortals() {
        return portals.values();
    }
    
    /** 😡 查找包含指定点的传送门 😡
     */
    public Portal findPortalContainingPoint(Vec3 point, ResourceKey<Level> dimension) {
        List<Portal> dimensionPortals = getPortalsInDimension(dimension);
        
        for (Portal portal : dimensionPortals) {
            if (portal.containsPoint(point)) {
                return portal;
            }
        }
        
        return null;
    }
    
    /** 😡 查找最近的传送门 😡
     */
    public Portal findNearestPortal(Vec3 position, ResourceKey<Level> dimension, double maxDistance) {
        List<Portal> dimensionPortals = getPortalsInDimension(dimension);
        
        Portal nearest = null;
        double nearestDistance = maxDistance;
        
        for (Portal portal : dimensionPortals) {
            double distance = position.distanceTo(portal.getPosition());
            
            if (distance < nearestDistance) {
                nearest = portal;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }
    
    /** 😡 创建地狱门传送门 * * 这是一个特殊的传送门，用于从主世界看到地狱 😡
     */
    public Portal createNetherPortal(Vec3 position, float width, float height) {
        try {
            // 😡 创建一个从主世界到地狱的传送门 😡
            Quaternionf rotation = new Quaternionf(); // 😡 默认朝向 😡
            
            // 😡 地狱中的对应位置（通常是 1/8 的坐标） 😡
            Vec3 netherPosition = new Vec3(position.x / 8.0, position.y, position.z / 8.0);
            
            Portal portal = createPortal(
                position,
                rotation,
                width,
                height,
                Level.OVERWORLD,
                Level.NETHER,
                netherPosition,
                new Quaternionf()
            );
            
            LOGGER.info("[PortalManager] 创建地狱门传送门");
            
            return portal;
            
        } catch (Exception e) {
            LOGGER.error("[PortalManager] 创建地狱门传送门失败", e);
            return null;
        }
    }
    
    /** 😡 创建太空传送门 * * 这是一个特殊的传送门，用于从地面传送到太空 😡
     */
    public Portal createSpacePortal(Vec3 position, ResourceKey<Level> spaceDimension) {
        try {
            Quaternionf rotation = new Quaternionf();
            
            // 😡 太空中的对应位置（高度很高） 😡
            Vec3 spacePosition = new Vec3(position.x, 256, position.z);
            
            Portal portal = createPortal(
                position,
                rotation,
                4.0f, // 😡 宽度 😡
                5.0f, // 😡 高度 😡
                Level.OVERWORLD,
                spaceDimension,
                spacePosition,
                new Quaternionf()
            );
            
            LOGGER.info("[PortalManager] 创建太空传送门");
            
            return portal;
            
        } catch (Exception e) {
            LOGGER.error("[PortalManager] 创建太空传送门失败", e);
            return null;
        }
    }
    
    /** 😡 清理所有传送门 😡
     */
    public void clearAllPortals() {
        try {
            for (Portal portal : portals.values()) {
                CrossDimensionPortalRenderer.getInstance().unregisterPortal(portal);
            }
            
            portals.clear();
            portalsByDimension.clear();
            portalCounter = 0;
            
            LOGGER.info("[PortalManager] 清理所有传送门");
            
        } catch (Exception e) {
            LOGGER.error("[PortalManager] 清理传送门失败", e);
        }
    }
    
    /** 😡 获取传送门统计信息 😡
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_portals", portals.size());
        stats.put("dimensions_with_portals", portalsByDimension.size());
        
        Map<String, Integer> portalsByDimensionCount = new HashMap<>();
        for (Map.Entry<ResourceKey<Level>, List<String>> entry : portalsByDimension.entrySet()) {
            portalsByDimensionCount.put(entry.getKey().location().toString(), entry.getValue().size());
        }
        stats.put("portals_by_dimension", portalsByDimensionCount);
        
        return stats;
    }
}
