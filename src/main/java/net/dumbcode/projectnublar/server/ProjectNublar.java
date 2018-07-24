package net.dumbcode.projectnublar.server;

import net.dumbcode.dumblibrary.client.animation.AnimatableRenderer;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.Velociraptor;
import net.dumbcode.projectnublar.server.dinosaur.data.CachedItems;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

@Mod.EventBusSubscriber
@Mod(modid = ProjectNublar.MODID, name = ProjectNublar.NAME, version = ProjectNublar.VERSION)
public class ProjectNublar
{
    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.0.1";

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;

    private static Logger logger;

    public static CreativeTabs TAB = new CreativeTabs("projectnublar") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ItemBlock.getItemFromBlock(Blocks.DEADBUSH)); // TODO: custom item
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, manager -> new AnimatableRenderer<>(manager, entity -> entity.getDinosaur().getModelContainer(),
                entity -> {
                    ResourceLocation regname = entity.getDinosaur().getRegName();
                    return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (entity.isMale() ? "male" : "female") + "_" + entity.getGrowthStage().name().toLowerCase(Locale.ROOT) + ".png");

                }));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for (Dinosaur dinosaur : DINOSAUR_REGISTRY.getValuesCollection()) {
            dinosaur.setCachedItems(new CachedItems(dinosaur));
        }
        ItemHandler.DINOSAUR_MEAT.registerOreNames();

        GameRegistry.registerTileEntity(BlockEntitySkeletalBuilder.class, new ResourceLocation(MODID, "skeletal_builder"));
    }

    @SubscribeEvent
    public static void createRegisteries(RegistryEvent.NewRegistry event) {
        DINOSAUR_REGISTRY = new RegistryBuilder<Dinosaur>()
                .setType(Dinosaur.class)
                .setName(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                .setDefaultKey(new ResourceLocation(ProjectNublar.MODID,"velociraptor"))
                .set(((key, isNetwork) -> Dinosaur.MISSING))
                .create();
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Dinosaur> event) {
        event.getRegistry().register(new Velociraptor().setRegistryName("projectnublar:velociraptor"));
    }

    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandProjectNublar());
    }
}
