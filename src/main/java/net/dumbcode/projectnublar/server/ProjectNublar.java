package net.dumbcode.projectnublar.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.projectnublar.client.ProjectNublarBlockRenderLayers;
import net.dumbcode.projectnublar.client.gui.icons.EnumWeatherIcons;
import net.dumbcode.projectnublar.client.particle.ProjectNublarParticleFactories;
import net.dumbcode.projectnublar.server.animation.AnimationFactorHandler;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.dumbcode.projectnublar.server.data.ProjectNublarBlockTagsProvider;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DataSerializerHandler;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.entity.system.impl.*;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.dumbcode.projectnublar.server.particles.ProjectNublarParticles;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.registry.EarlyRegistryEvent;
import net.dumbcode.projectnublar.server.tablet.TabletModuleHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.utils.JsonHandlers;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber
@Mod(ProjectNublar.MODID)
public class ProjectNublar {

    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.1.25";
    public static final String DUMBLIBRARY_VERSION = "0.2.4";

    public static final boolean DEBUG = false;

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;
    public static IForgeRegistry<Plant> PLANT_REGISTRY;


    private static Logger logger = LogManager.getLogger(MODID);


    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public ProjectNublar() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(this::preInit);

        BlockHandler.REGISTER.register(bus);
        ProjectNublarBlockEntities.REGISTER.register(bus);
        ProjectNublarContainers.REGISTER.register(bus);
        ProjectNublarParticles.REGISTER.register(bus);
        TabletModuleHandler.DR.register(bus);
        EntityHandler.REGISTER.register(bus);

        bus.addGenericListener(Block.class, Plant::registerBlocks);
        bus.addGenericListener(Item.class, ItemHandler::registerAllItemBlocks);

        forgeBus.addListener(this::gatherData);

//        forgeBus.addGenericListener(Dinosaur.class, EventPriority.LOWEST, DinosaurBaseBlock::onDinosaurRegistryFinished);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(ProjectNublarParticleFactories::onParticleFactoriesRegister);

            forgeBus.addListener(ProjectNublarBlockRenderLayers::setRenderLayers);

        });
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeServer()) {
            gen.addProvider(new ProjectNublarBlockTagsProvider(gen, helper));
        }
    }

    public void preInit(FMLCommonSetupEvent event) {
//        registerJsonDinosaurs();


        DINOSAUR_REGISTRY.forEach(Dinosaur::attachDefaultComponents);
        PLANT_REGISTRY.forEach(Plant::attachComponents);

        registerPackets();

        DataSerializerHandler.register();
        if(FMLEnvironment.dist.isClient()) {
            EnumWeatherIcons.register();
            TabletBackground.registerDefaults();
            ProjectNublarContainers.registerScreens();
        }
        AnimationFactorHandler.register();

        for (Dinosaur dinosaur : DINOSAUR_REGISTRY.getValues()) {
            ResourceLocation regName = dinosaur.getRegName();
            for (AgeStage orderedAge : dinosaur.getAttacher().getStorage(ComponentHandler.AGE).getOrderedAges()) {
                Map<String, AnimationContainer> container = dinosaur.getModelContainer();

                container.computeIfAbsent(orderedAge.getModelStage(),
                    s -> new AnimationContainer(new ResourceLocation(regName.getNamespace(), regName.getPath() + "/" + s)));

            }
        }

//        GameRegistry.registerTileEntity(SkeletalBuilderBlockEntity.class, new ResourceLocation(MODID, "skeletal_builder"));
//        GameRegistry.registerTileEntity(FossilProcessorBlockEntity.class, new ResourceLocation(MODID, "fossil_processor"));
//        GameRegistry.registerTileEntity(DrillExtractorBlockEntity.class, new ResourceLocation(MODID, "drill_extractor"));
//        GameRegistry.registerTileEntity(SequencingSynthesizerBlockEntity.class, new ResourceLocation(MODID, "sequencing_synthesizer"));
//        GameRegistry.registerTileEntity(EggPrinterBlockEntity.class, new ResourceLocation(MODID, "egg_printer"));
//        GameRegistry.registerTileEntity(IncubatorBlockEntity.class, new ResourceLocation(MODID, "incubator"));
//        GameRegistry.registerTileEntity(CoalGeneratorBlockEntity.class, new ResourceLocation(MODID, "coal_generator"));
//        GameRegistry.registerTileEntity(BlockEntityElectricFencePole.class, new ResourceLocation(MODID, "electric_fence_pole"));
//        GameRegistry.registerTileEntity(BlockEntityElectricFence.class, new ResourceLocation(MODID, "electric_fence"));
//        GameRegistry.registerTileEntity(BlockCreativePowerSource.TileEntityCreativePowerSource.class, new ResourceLocation(MODID, "creative_power"));
//        GameRegistry.registerTileEntity(TrackingBeaconBlockEntity.class, new ResourceLocation(MODID, "tracking_beacon"));
//        GameRegistry.registerTileEntity(PylonHeadBlockEntity.class, new ResourceLocation(MODID, "pylon_head"));


        // TODO: Remove, debug only
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        DINOSAUR_REGISTRY.getValues().forEach(dino -> {
            File jsonFile = new File("./mods/projectnublar/debug/" + dino.getRegName().getPath() + ".json");
            if (!jsonFile.getParentFile().exists()) {
                jsonFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(dino, writer);
            } catch (IOException e) {
                logger.warn("There was an issue writing to {}", jsonFile.getName());
            }
        });
    }

    //TODO: move these to deferred register.
    @SubscribeEvent
    public static void createRegistries(RegistryEvent.NewRegistry event) {
        DINOSAUR_REGISTRY = new RegistryBuilder<Dinosaur>()
                .setType(Dinosaur.class)
                .setName(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "missing"))
                .set(((key, isNetwork) -> DinosaurHandler.TYRANNOSAURUS))
                .create();

        PLANT_REGISTRY = new RegistryBuilder<Plant>()
                .setType(Plant.class)
                .setName(new ResourceLocation(ProjectNublar.MODID, "plant"))
                .create();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.post(new EarlyRegistryEvent<>(DINOSAUR_REGISTRY));
        bus.post(new EarlyRegistryEvent<>(PLANT_REGISTRY));
    }

    @SubscribeEvent
    public static void register(RegisterSystemsEvent event) {
        event.registerSystem(new AgeSystem());
        event.registerSystem(new MultipartSystem());
        event.registerSystem(new DinosaurEggLayingSystem());
        event.registerSystem(new TrackingSystem());
        event.registerSystem(new MetabolismSystem());
        event.registerSystem(new MoodSystem());
        event.registerSystem(new DefenseSystem());
        event.registerSystem(new DinosaurMovementSystem());
    }

    public static Logger getLogger() {
        return logger;
    }


    private static void registerJsonDinosaurs() {
        GsonBuilder builder = new GsonBuilder();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        JsonUtil.registerModJsons(DINOSAUR_REGISTRY, gson, ProjectNublar.MODID, "dinosaurs");
    }

    private void registerPackets() {

        //Now you might be asking where the packets 0-8 are, they simply got moved to dumb lib and i don't want to change the packet names
        NETWORK.registerMessage(new BChangeGlobalRotation.Handler(), BChangeGlobalRotation.class, 9, Side.SERVER);
        NETWORK.registerMessage(new S10ChangeGlobalRotation.Handler(), S10ChangeGlobalRotation.class, 10, Side.CLIENT);
        NETWORK.registerMessage(new BUpdatePoleList.Handler(), BUpdatePoleList.class, 11, Side.SERVER);
        NETWORK.registerMessage(new S12UpdatePoleList.Handler(), S12UpdatePoleList.class, 12, Side.CLIENT);
        NETWORK.registerMessage(new C2SVehicleInputStateUpdated.Handler(), C2SVehicleInputStateUpdated.class, 13, Side.SERVER);
        NETWORK.registerMessage(new C2SSequencingSynthesizerSelectChange.Handler(), C2SSequencingSynthesizerSelectChange.class, 14, Side.SERVER);
        NETWORK.registerMessage(new S15SyncSequencingSynthesizerSelectChange.Handler(), S15SyncSequencingSynthesizerSelectChange.class, 15, Side.CLIENT);
        NETWORK.registerMessage(new C16DisplayTabbedGui.Handler(), C16DisplayTabbedGui.class, 16, Side.SERVER);
        NETWORK.registerMessage(new S2CMachinePositionDirty.Handler(), S2CMachinePositionDirty.class, 17, Side.CLIENT);
        NETWORK.registerMessage(new C2SChangeContainerTab.Handler(), C2SChangeContainerTab.class, 18, Side.SERVER);
        NETWORK.registerMessage(new S19SetGuiWindow.Handler(), S19SetGuiWindow.class, 19, Side.CLIENT);
        NETWORK.registerMessage(new S2CRegenFenceCache.Handler(), S2CRegenFenceCache.class, 20, Side.CLIENT);
        NETWORK.registerMessage(new S21SpawnParticle.Handler(), S21SpawnParticle.class, 21, Side.CLIENT);
        NETWORK.registerMessage(new S2CStartTrackingTabletHandshake.Handler(), S2CStartTrackingTabletHandshake.class, 22, Side.CLIENT);
        NETWORK.registerMessage(new C2SConfirmTrackingTablet.Handler(), C2SConfirmTrackingTablet.class, 23, Side.SERVER);
        NETWORK.registerMessage(new S2STrackingTabletUpdateChunk.Handler(), S2STrackingTabletUpdateChunk.class, 24, Side.CLIENT);
        NETWORK.registerMessage(new C25StopTrackingTablet.Handler(), C25StopTrackingTablet.class, 25, Side.SERVER);
        NETWORK.registerMessage(new S2COpenTablet.Handler(), S2COpenTablet.class, 26, Side.CLIENT);
        NETWORK.registerMessage(new C2SInstallModule.Handler(), C2SInstallModule.class, 27, Side.SERVER);
        NETWORK.registerMessage(new C2STabletModuleClicked.Handler(), C2STabletModuleClicked.class, 28, Side.SERVER);
        NETWORK.registerMessage(new S2COpenTabletModule.Handler(), S2COpenTabletModule.class, 29, Side.CLIENT);
        NETWORK.registerMessage(new C2STrackingTabletEntryClicked.Handler(), C2STrackingTabletEntryClicked.class, 30, Side.SERVER);
        NETWORK.registerMessage(new C2STrackingBeaconData.Handler(), C2STrackingBeaconData.class, 31, Side.SERVER);
        NETWORK.registerMessage(new S2CSetTrackingDataList.Handler(), S2CSetTrackingDataList.class, 32, Side.CLIENT);
        NETWORK.registerMessage(new C2SSetTabletBackground.Handler(), C2SSetTabletBackground.class, 33, Side.SERVER);
        NETWORK.registerMessage(new CS2UploadPhotoBackgroundImage.Handler(), CS2UploadPhotoBackgroundImage.class, 34, Side.SERVER);
        NETWORK.registerMessage(new C2SPhotoBackgroundRequestAllIcons.Handler(), C2SPhotoBackgroundRequestAllIcons.class, 35, Side.SERVER);
        NETWORK.registerMessage(new S2CRequestBackgroundIconHeaders.Handler(), S2CRequestBackgroundIconHeaders.class, 36, Side.CLIENT);
        NETWORK.registerMessage(new C2SRequestBackgroundImage.Handler(), C2SRequestBackgroundImage.class, 37, Side.SERVER);
        NETWORK.registerMessage(new S2CSyncBackgroundImage.Handler(), S2CSyncBackgroundImage.class, 38, Side.CLIENT);
        NETWORK.registerMessage(new S2CSyncBackgroundIcon.Handler(), S2CSyncBackgroundIcon.class, 39, Side.CLIENT);
        NETWORK.registerMessage(new C2SRequestPhotoBackgroundIcon.Handler(), C2SRequestPhotoBackgroundIcon.class, 40, Side.SERVER);
        NETWORK.registerMessage(new C2SPlaceIncubatorEgg.Handler(), C2SPlaceIncubatorEgg.class, 41, Side.SERVER);
        NETWORK.registerMessage(new S2CSyncMachineProcesses.Handler(), S2CSyncMachineProcesses.class, 42, Side.CLIENT);
        NETWORK.registerMessage(new S43SyncMachineStack.Handler(), S43SyncMachineStack.class, 43, Side.CLIENT);
        NETWORK.registerMessage(new S44SyncOpenedUsers.Handler(), S44SyncOpenedUsers.class, 44, Side.CLIENT);
    }
}
