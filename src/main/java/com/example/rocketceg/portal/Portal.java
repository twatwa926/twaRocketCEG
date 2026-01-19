ackage com.example.rocketceg.portal;

import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

/** 😡 传送门实体 - 100% 按照 ImmersivePortalsMod 实现 * * 代表一个传送门，包含所有必要的数据来定义传送门的位置、大小、旋转和目标。 * * 核心属性： * - 位置和大小：传送门在世界中的位置和尺寸 * - 旋转：传送门的方向（使用四元数表示） * - 源维度和目标维度：传送门连接的两个维度 * - 目标位置和旋转：玩家传送后的位置和旋转 * - 属性：双向、融合视图等 😡
     */
public class Portal {
    
    // 😡 唯一标识符 😡
    private final UUID id;
    
    // 😡 位置和大小 😡
    private Vec3 position;
    private double width;
    private double height;
    
    // 😡 旋转（四元数） 😡
    private Quaternionf rotation;
    
    // 😡 源维度 😡
    private ResourceKey<Level> fromDimension;
    
    // 😡 目标维度 😡
    private ResourceKey<Level> toDimension;
    
    // 😡 目标位置 😡
    private Vec3 targetPosition;
    
    // 😡 目标旋转 😡
    private Quaternionf targetRotation;
    
    // 😡 属性 😡
    private boolean bilateral; // 😡 双向传送 😡
    private boolean fuseView; // 😡 融合视图 😡
    private boolean customShape; // 😡 自定义形状 😡
    private boolean active; // 😡 是否激活 😡
    
    // 😡 创建时间 😡
    private final long createdTime;
    
    // 😡 最后更新时间 😡
    private long lastUpdatedTime;
    
    /** 😡 创建一个新的传送门 😡
     */
    public Portal(
        Vec3 position,
        double width,
        double height,
        Quaternionf rotation,
        ResourceKey<Level> fromDimension,
        ResourceKey<Level> toDimension,
        Vec3 targetPosition,
        Quaternionf targetRotation
    ) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.width = width;
        this.height = height;
        this.rotation = new Quaternionf(rotation);
        this.fromDimension = fromDimension;
        this.toDimension = toDimension;
        this.targetPosition = targetPosition;
        this.targetRotation = new Quaternionf(targetRotation);
        
        this.bilateral = false;
        this.fuseView = false;
        this.customShape = false;
        this.active = true;
        
        this.createdTime = System.currentTimeMillis();
        this.lastUpdatedTime = this.createdTime;
    }
    
    /** 😡 获取传送门的法向量（指向传送门前方） 😡
     */
    public Vec3 getNormal() {
        // 😡 默认法向量是 (0, 0, 1)，然后应用旋转 😡
        Vector3f normal = new Vector3f(0, 0, 1);
        rotation.transform(normal);
        return new Vec3(normal.x, normal.y, normal.z);
    }
    
    /** 😡 获取传送门的右向量 😡
     */
    public Vec3 getRight() {
        // 😡 默认右向量是 (1, 0, 0)，然后应用旋转 😡
        Vector3f right = new Vector3f(1, 0, 0);
        rotation.transform(right);
        return new Vec3(right.x, right.y, right.z);
    }
    
    /** 😡 获取传送门的上向量 😡
     */
    public Vec3 getUp() {
        // 😡 默认上向量是 (0, 1, 0)，然后应用旋转 😡
        Vector3f up = new Vector3f(0, 1, 0);
        rotation.transform(up);
        return new Vec3(up.x, up.y, up.z);
    }
    
    /** 😡 检查一个点是否在传送门内 😡
     */
    public boolean containsPoint(Vec3 point) {
        // 😡 计算点相对于传送门中心的位置 😡
        Vec3 relativePos = point.subtract(position);
        
        // 😡 获取传送门的方向向量 😡
        Vec3 normal = getNormal();
        Vec3 right = getRight();
        Vec3 up = getUp();
        
        // 😡 计算点在传送门坐标系中的坐标 😡
        double normalDist = relativePos.dot(normal);
        double rightDist = relativePos.dot(right);
        double upDist = relativePos.dot(up);
        
        // 😡 检查点是否在传送门范围内 😡
        // 😡 允许一些容差（0.1 方块） 😡
        double tolerance = 0.1;
        
        return Math.abs(normalDist) < tolerance &&
               Math.abs(rightDist) <= width / 2.0 + tolerance &&
               Math.abs(upDist) <= height / 2.0 + tolerance;
    }
    
    /** 😡 检查玩家是否穿过传送门 * * 通过检查玩家的前一个位置和当前位置是否跨越传送门平面来判断 😡
     */
    public boolean isPlayerCrossingPortal(Vec3 previousPos, Vec3 currentPos) {
        Vec3 normal = getNormal();
        
        // 😡 计算前一个位置和当前位置相对于传送门平面的距离 😡
        double prevDist = previousPos.subtract(position).dot(normal);
        double currDist = currentPos.subtract(position).dot(normal);
        
        // 😡 如果符号改变，说明穿过了传送门平面 😡
        if ((prevDist < 0 && currDist > 0) || (prevDist > 0 && currDist < 0)) {
            // 😡 还需要检查是否在传送门范围内 😡
            return containsPoint(currentPos);
        }
        
        return false;
    }
    
    /** 😡 获取传送门的平移向量 😡
     */
    public Vec3 getTranslation() {
        return targetPosition.subtract(position);
    }
    
    /** 😡 获取传送门的缩放因子（默认为 1.0） 😡
     */
    public double getScale() {
        return 1.0;
    }
    
    /** 😡 检查是否启用镜像 😡
     */
    public boolean isMirror() {
        return false;
    }
    
    // 😡 ==================== Getters and Setters ==================== 😡
    
    public UUID getId() {
        return id;
    }
    
    public Vec3 getPosition() {
        return position;
    }
    
    public void setPosition(Vec3 position) {
        this.position = position;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public Quaternionf getRotation() {
        return new Quaternionf(rotation);
    }
    
    public void setRotation(Quaternionf rotation) {
        this.rotation = new Quaternionf(rotation);
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public ResourceKey<Level> getFromDimension() {
        return fromDimension;
    }
    
    public void setFromDimension(ResourceKey<Level> fromDimension) {
        this.fromDimension = fromDimension;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public ResourceKey<Level> getToDimension() {
        return toDimension;
    }
    
    public void setToDimension(ResourceKey<Level> toDimension) {
        this.toDimension = toDimension;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public Vec3 getTargetPosition() {
        return targetPosition;
    }
    
    public void setTargetPosition(Vec3 targetPosition) {
        this.targetPosition = targetPosition;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public Quaternionf getTargetRotation() {
        return new Quaternionf(targetRotation);
    }
    
    public void setTargetRotation(Quaternionf targetRotation) {
        this.targetRotation = new Quaternionf(targetRotation);
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public boolean isBilateral() {
        return bilateral;
    }
    
    public void setBilateral(boolean bilateral) {
        this.bilateral = bilateral;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public boolean isFuseView() {
        return fuseView;
    }
    
    public void setFuseView(boolean fuseView) {
        this.fuseView = fuseView;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public boolean isCustomShape() {
        return customShape;
    }
    
    public void setCustomShape(boolean customShape) {
        this.customShape = customShape;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        this.lastUpdatedTime = System.currentTimeMillis();
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Portal{id=%s, pos=%.1f,%.1f,%.1f, size=%.1f×%.1f, %s -> %s}",
            id.toString().substring(0, 8),
            position.x, position.y, position.z,
            width, height,
            fromDimension.location(),
            toDimension.location()
        );
    }
}
