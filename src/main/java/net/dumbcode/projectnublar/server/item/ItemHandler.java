package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public final class ItemHandler {

    public static final ItemDinosaurMeat DINOSAUR_MEAT = getNonNull();
    public static final DinosaurSpawnEgg SPAWN_EGG = getNonNull();
    public static final FossilItem FOSSIL = getNonNull();

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemDinosaurMeat()
                        .setRegistryName("dinosaur_meat")
                        .setUnlocalizedName("dinosaur_meat")
                        .setCreativeTab(ProjectNublar.TAB),

                new DinosaurSpawnEgg()
                        .setRegistryName("spawn_egg")
                        .setUnlocalizedName("spawn_egg")
                        .setCreativeTab(ProjectNublar.TAB),

                new FossilItem()
                        .setRegistryName("fossil")
                        .setUnlocalizedName("fossil")
                        .setCreativeTab(ProjectNublar.TAB)
        );
        for (Block block : ForgeRegistries.BLOCKS) {
            if(block instanceof IItemBlock) {
                event.getRegistry().register(((IItemBlock)block).createItem()
                        .setRegistryName(block.getRegistryName())
                        .setUnlocalizedName(block.getUnlocalizedName().substring("tile.".length()))
                        .setCreativeTab(block.getCreativeTabToDisplayOn()));
            }
        }
    }

//    @Nonnull
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
