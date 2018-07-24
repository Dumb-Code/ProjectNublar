package net.dumbcode.projectnublar.server.block;

import com.sun.istack.internal.NotNull;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class BlockHandler {

    public static final SkeletalBuilder SKELETAL_BUILDER = getNonNull();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new SkeletalBuilder().setRegistryName("skeletal_builder").setUnlocalizedName("skeletal_builder")
        );
    }

//    @Nonnull//TODO: fix
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
