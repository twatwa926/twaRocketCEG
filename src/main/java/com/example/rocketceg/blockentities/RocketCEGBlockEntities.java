ackage com.example.rocketceg.blockentities;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 😡 RocketCEG 模组的 BlockEntity 注册类 😡
     */
public class RocketCEGBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RocketCEGMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD_BE = 
        BLOCK_ENTITIES.register("launch_pad", () -> 
            BlockEntityType.Builder.of(LaunchPadBlockEntity::new, 
                com.example.rocketceg.registry.RocketCEGBlocks.LAUNCH_PAD.get()).build(null));

    public static final RegistryObject<BlockEntityType<RocketEngineBlockEntity>> ROCKET_ENGINE_BE = 
        BLOCK_ENTITIES.register("rocket_engine", () -> 
            BlockEntityType.Builder.of(RocketEngineBlockEntity::new, 
                com.example.rocketceg.registry.RocketCEGBlocks.ROCKET_ENGINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RocketFuelTankBlockEntity>> ROCKET_FUEL_TANK_BE = 
        BLOCK_ENTITIES.register("rocket_fuel_tank", () -> 
            BlockEntityType.Builder.of(RocketFuelTankBlockEntity::new, 
                com.example.rocketceg.registry.RocketCEGBlocks.ROCKET_FUEL_TANK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
