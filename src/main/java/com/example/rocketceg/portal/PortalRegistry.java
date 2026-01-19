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

/** 😡 传送门注册表 - 管理所有传送门 * * 这是 ImmersivePortalsMod 的核心管理系统，负责： * 1. 创建和删除传送门 * 2. 按维度索引传送门 * 3. 查询和搜索传送门 * 4. 特殊传送门支持（地狱门、太空门等） * 5. 统计和监控 😡
     */
public class PortalRegistry {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    private static PortalRegistry INSTANCE;
    
    // 😡 所有传送门（按 ID 索引） 😡
    private final Map<UUID, Portal> portalsById = new ConcurrentHashMap<>();
    
    // 😡 按源维度索引的传送门 😡
    private final Map<ResourceKey<Level>, List<Portal>> portalsByFromDimension = new ConcurrentHashMap<>();
    
    // 😡 按目标维度索引的传送门 😡
    private final Map<ResourceKey<Level>, List<Portal>> portalsByToDimension = new ConcurrentHashMap<>();
    
    // 😡 特殊传送门（地狱门、太空门等） 😡
    private final Map<String, Portal> specialPortals = new ConcurrentHashMap<>();
    
    private PortalRegistry() {}
    
    public static PortalRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (PortalRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PortalRegistry();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 创建一个新的传送门 😡
     */
    public Portal createPortal(
        Vec3 position,
        double width,
        double height,
        Quaternionf rotation,
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
            
            // 😡 注册传送门 😡
            portalsById.put(portal.getId(), portal);
            portalsByFromDimension.computeIfAbsent(fromDimension, k -> new ArrayList<>()).add(portal);
            portalsByToDimension.computeIfAbsent(toDimension, k -> new ArrayList<>()).add(portal);
            
            LOGGER.info("[PortalRegistry] 创建传送门: {} (ID: {})", portal, portal.getId());
            
            return portal;
            
        } catch (Exception e) {
            LOGGER.error("[PortalRegistry] 创建传送门失败", e);
            return null;
        }
    }
    
    /** 😡 删除一个传送门 😡
     */
    public boolean removePortal(UUID portalId) {
        try {
            Portal portal = portalsById.remove(portalId);
            
            if (portal == null) {
                LOGGER.warn("[PortalRegistry] 传送门不存在: {}", portalId);
                return false;
            }
            
            // 😡 从维度索引中移除 😡
            List<Portal> fromList = portalsByFromDimension.get(portal.getFromDimension());
            if (fromList != null) {
                fromList.remove(portal);
            }
            
            List<Portal> toList = portalsByToDimension.get(portal.getToDimension());
            if (toList != null) {
                toList.remove(portal);
            }
            
            // 😡 从特殊传送门中移除 😡
            specialPortals.values().remove(portal);
            
            LOGGER.info("[PortalRegistry] 删除传送门: {} (ID: {})", portal, portalId);
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("[PortalRegistry] 删除传送门失败", e);
            return false;
        }
    }
    
    /** 😡 获取一个传送门 😡
     */
    public Portal getPortal(UUID portalId) {
        return portalsById.get(portalId);
    }
    
    /** 😡 获取源维度中的所有传送门 😡
     */
    public List<Portal> getPortalsInDimension(ResourceKey<Level> dimension) {
        return new ArrayList<>(portalsByFromDimension.getOrDefault(dimension, new ArrayList<>()));
    }
    
    /** 😡 查找包含指定点的传送门 😡
     */
    public Portal findPortalContainingPoint(Vec3 point, ResourceKey<Level> dimension) {
        List<Portal> portals = portalsByFromDimension.getOrDefault(dimension, new ArrayList<>());
        
        for (Portal portal : portals) {
            if (portal.isActive() && portal.containsPoint(point)) {
                return portal;
            }
        }
        
        return null;
    }
    
    /** 😡 查找玩家穿过的传送门 😡
     */
    public Portal findPortalPlayerCrossing(Vec3 previousPos, Vec3 currentPos, ResourceKey<Level> dimension) {
        List<Portal> portals = portalsByFromDimension.getOrDefault(dimension, new ArrayList<>());
        
        for (Portal portal : portals) {
            if (portal.isActive() && portal.isPlayerCrossingPortal(previousPos, currentPos)) {
                return portal;
            }
        }
        
        return null;
    }
    
    /** 😡 获取所有传送门 😡
     */
    public Collection<Portal> getAllPortals() {
        return new ArrayList<>(portalsById.values());
    }
    
    /** 😡 获取所有活跃的传送门 😡
     */
    public List<Portal> getActivePortals() {
        List<Portal> active = new ArrayList<>();
        for (Portal portal : portalsById.values()) {
            if (portal.isActive()) {
                active.add(portal);
            }
        }
        return active;
    }
    
    /** 😡 注册特殊传送门（地狱门、太空门等） 😡
     */
    public void registerSpecialPortal(String name, Portal portal) {
        specialPortals.put(name, portal);
        LOGGER.info("[PortalRegistry] 注册特殊传送门: {} ({})", name, portal.getId());
    }
    
    /** 😡 获取特殊传送门 😡
     */
    public Portal getSpecialPortal(String name) {
        return specialPortals.get(name);
    }
    
    /** 😡 获取所有特殊传送门 😡
     */
    public Map<String, Portal> getAllSpecialPortals() {
        return new HashMap<>(specialPortals);
    }
    
    /** 😡 清空所有传送门 😡
     */
    public void clear() {
        portalsById.clear();
        portalsByFromDimension.clear();
        portalsByToDimension.clear();
        specialPortals.clear();
        LOGGER.info("[PortalRegistry] 清空所有传送门");
    }
    
    /** 😡 获取传送门统计信息 😡
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_portals", portalsById.size());
        stats.put("active_portals", (int) portalsById.values().stream().filter(Portal::isActive).count());
        stats.put("special_portals", specialPortals.size());
        stats.put("dimensions_with_portals", portalsByFromDimension.size());
        return stats;
    }
    
    /** 😡 获取调试信息 😡
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Portal Registry Debug Info ===\n");
        sb.append(String.format("Total Portals: %d\n", portalsById.size()));
        sb.append(String.format("Active Portals: %d\n", 
            portalsById.values().stream().filter(Portal::isActive).count()));
        sb.append(String.format("Special Portals: %d\n", specialPortals.size()));
        sb.append(String.format("Dimensions: %d\n", portalsByFromDimension.size()));
        
        if (!portalsById.isEmpty()) {
            sb.append("\nPortals:\n");
            for (Portal portal : portalsById.values()) {
                sb.append(String.format("  - %s (Active: %s)\n", portal, portal.isActive()));
            }
        }
        
        return sb.toString();
    }
}
