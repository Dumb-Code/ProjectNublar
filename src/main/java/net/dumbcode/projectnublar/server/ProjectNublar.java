package net.dumbcode.projectnublar.server;

import net.dumbcode.dumblibrary.client.animation.AnimatableRenderer;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.Velociraptor;
import net.dumbcode.projectnublar.server.dinosaur.data.CachedItems;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = ProjectNublar.MODID, name = ProjectNublar.NAME, version = ProjectNublar.VERSION)
public class ProjectNublar
{
    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.0.1";

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;

    private static Logger logger;

    @Mod.Instance(MODID)
    public static ProjectNublar INSTANCE;

    public static SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    public static CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ItemBlock.getItemFromBlock(Blocks.DEADBUSH)); // TODO: custom item
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, manager -> new AnimatableRenderer<>(manager, entity -> entity.getDinosaur().getModelContainer(), entity -> {
            Dinosaur dinosaur = entity.getDinosaur();
            return dinosaur.getTextureLocation(entity);
        }));
        registerPackets();
    }

    private void registerPackets() {
        NETWORK.registerMessage(C0MoveSelectedSkeletalPart.Handler.class, C0MoveSelectedSkeletalPart.class, 0, Side.SERVER);
        NETWORK.registerMessage(S1UpdateSkeletalBuilder.Handler.class, S1UpdateSkeletalBuilder.class, 1, Side.CLIENT);
        NETWORK.registerMessage(C2SkeletalMovement.Handler.class, C2SkeletalMovement.class, 2, Side.SERVER);
        NETWORK.registerMessage(S3HistoryRecord.Handler.class, S3HistoryRecord.class, 3, Side.CLIENT);
        NETWORK.registerMessage(C4MoveInHistory.Handler.class, C4MoveInHistory.class, 4, Side.SERVER);
        NETWORK.registerMessage(S5UpdateHistoryIndex.Handler.class, S5UpdateHistoryIndex.class, 5, Side.CLIENT);
        NETWORK.registerMessage(C6ResetPose.Handler.class, C6ResetPose.class, 6, Side.SERVER);
        NETWORK.registerMessage(S7FullPoseChange.Handler.class, S7FullPoseChange.class, 7, Side.CLIENT);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
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
        MinecraftForge.EVENT_BUS.post(new RegisterDinosaurEvent(DINOSAUR_REGISTRY));
    }

    @SubscribeEvent
    public static void register(RegisterDinosaurEvent event) {
        event.getRegistry().register(new Velociraptor().setRegistryName("projectnublar:missing")); // TODO: custom class?
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
