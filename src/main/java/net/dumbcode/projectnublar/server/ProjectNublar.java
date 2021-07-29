package net.dumbcode.projectnublar.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.dumblibrary.server.registry.PreBlockRegistryEvent;
import net.dumbcode.projectnublar.client.ProjectNublarBlockRenderLayers;
import net.dumbcode.projectnublar.client.gui.icons.EnumWeatherIcons;
import net.dumbcode.projectnublar.client.particle.ProjectNublarParticleFactories;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityEggPrinterRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityIncubatorRenderer;
import net.dumbcode.projectnublar.server.animation.AnimationFactorHandler;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.BlockPylonPole;
import net.dumbcode.projectnublar.server.block.entity.ProjectNublarBlockEntities;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.dumbcode.projectnublar.server.data.ProjectNublarBlockTagsProvider;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.dna.GeneticHandler;
import net.dumbcode.projectnublar.server.dna.ProjectNublarGeneticRegistry;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DataSerializerHandler;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.entity.system.impl.*;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.dumbcode.projectnublar.server.particles.ProjectNublarParticles;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.plants.PlantHandler;
import net.dumbcode.projectnublar.server.recipes.crafting.ProjectNublarRecipesSerializers;
import net.dumbcode.projectnublar.server.sounds.SoundHandler;
import net.dumbcode.projectnublar.server.tablet.TabletModuleHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.utils.JsonHandlers;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
@Mod(ProjectNublar.MODID)
public class ProjectNublar {

    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.1.25";
    public static final String DUMBLIBRARY_VERSION = "0.2.4";

    public static final boolean DEBUG = true;


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
        ItemHandler.REGISTER.register(bus);
        ProjectNublarBlockEntities.REGISTER.register(bus);
        ProjectNublarContainers.REGISTER.register(bus);
        ProjectNublarParticles.REGISTER.register(bus);
        TabletModuleHandler.DR.register(bus);
        EntityHandler.REGISTER.register(bus);
        SoundHandler.REGISTER.register(bus);
        GeneticHandler.REGISTER.register(bus);
        DinosaurHandler.REGISTER.register(bus);
        PlantHandler.REGISTER.register(bus);
        ProjectNublarRecipesSerializers.REGISTER.register(bus);
        ComponentHandler.REGISTER.register(bus);

        bus.addGenericListener(Block.class, Plant::registerBlocks);
        bus.addGenericListener(Item.class, ItemHandler::registerAllItemBlocks);

        forgeBus.addListener(this::gatherData);
        forgeBus.addListener(BlockPylonPole::onBlockBreak);
        bus.addListener(EntityHandler::onAttributes);

        if(FMLEnvironment.dist == Dist.CLIENT) {
            this.clientSetup();
        }

        //We want this to be called BEFORE the actual block registry, but after the early registries.
        bus.addListener((PreBlockRegistryEvent.Normal event) -> {
            DinosaurHandler.getRegistry().forEach(Dinosaur::attachDefaultComponents);
            PlantHandler.getRegistry().forEach(Plant::attachComponents);
        });
    }

    public void clientSetup() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(ProjectNublarParticleFactories::onParticleFactoriesRegister);
        bus.addListener(this::clientPreInit);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeServer()) {
            gen.addProvider(new ProjectNublarBlockTagsProvider(gen, helper));
        }
    }

    public void clientPreInit(FMLClientSetupEvent event) {
        ProjectNublarBlockRenderLayers.setRenderLayers();
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) event.getMinecraftSupplier().get().getResourceManager();
        resourceManager.registerReloadListener(create(BlockEntityEggPrinterRenderer::onResourceManagerReload, VanillaResourceType.MODELS));
        resourceManager.registerReloadListener(create(EnumDinosaurEggTypes::onResourceManagerReload, null));
        resourceManager.registerReloadListener(create(BlockEntityIncubatorRenderer::onResourceManagerReload, VanillaResourceType.MODELS));
    }

    private static ISelectiveResourceReloadListener create(Consumer<IResourceManager> consumer, IResourceType type) {
        return new ISelectiveResourceReloadListener() {
            @Override
            public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
                if(type == null || resourcePredicate.test(type)) {
                    consumer.accept(resourceManager);
                }
            }

            @Nullable
            @Override
            public IResourceType getResourceType() {
                return type;
            }
        };
    }

    public void preInit(FMLCommonSetupEvent event) {
        ProjectNublarGeneticRegistry.register();

        for (Dinosaur dinosaur : DinosaurHandler.getRegistry().getValues()) {
            ResourceLocation regName = dinosaur.getRegName();
            for (AgeStage orderedAge : dinosaur.getAttacher().getStorage(ComponentHandler.AGE).getOrderedAges()) {
                Map<String, AnimationContainer> container = dinosaur.getModelContainer();

                container.computeIfAbsent(orderedAge.getModelStage(),
                    s -> new AnimationContainer(new ResourceLocation(regName.getNamespace(), regName.getPath() + "/" + s)));

            }
        }
//        registerJsonDinosaurs();

        registerPackets();

        DataSerializerHandler.register();
        if(FMLEnvironment.dist.isClient()) {
            EnumWeatherIcons.register();
            TabletBackground.registerDefaults();
            ProjectNublarContainers.registerScreens();
        }
        AnimationFactorHandler.register();

        // TODO: Remove, debug only
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        DinosaurHandler.getRegistry().getValues().forEach(dino -> {
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
        JsonUtil.registerModJsons(DinosaurHandler.getRegistry(), gson, ProjectNublar.MODID, "dinosaurs");
    }

    private void registerPackets() {

        NETWORK.registerMessage(0, BChangeGlobalRotation.class, BChangeGlobalRotation::toBytes, BChangeGlobalRotation::fromBytes, BChangeGlobalRotation::handle);
        NETWORK.registerMessage(1, BUpdatePoleList.class, BUpdatePoleList::toBytes, BUpdatePoleList::fromBytes, BUpdatePoleList::handle);
        NETWORK.registerMessage(2, C2SChangeContainerTab.class, C2SChangeContainerTab::toBytes, C2SChangeContainerTab::fromBytes, C2SChangeContainerTab::handle);
        NETWORK.registerMessage(3, C2SConfirmTrackingTablet.class, C2SConfirmTrackingTablet::toBytes, C2SConfirmTrackingTablet::fromBytes, C2SConfirmTrackingTablet::handle);
        NETWORK.registerMessage(4, C2SInstallModule.class, C2SInstallModule::toBytes, C2SInstallModule::fromBytes, C2SInstallModule::handle);
        NETWORK.registerMessage(5, C2SPhotoBackgroundRequestAllIcons.class, C2SPhotoBackgroundRequestAllIcons::toBytes, C2SPhotoBackgroundRequestAllIcons::fromBytes, C2SPhotoBackgroundRequestAllIcons::handle);
        NETWORK.registerMessage(6, C2SPlaceIncubatorEgg.class, C2SPlaceIncubatorEgg::toBytes, C2SPlaceIncubatorEgg::fromBytes, C2SPlaceIncubatorEgg::handle);
        NETWORK.registerMessage(7, C2SRequestBackgroundImage.class, C2SRequestBackgroundImage::toBytes, C2SRequestBackgroundImage::fromBytes, C2SRequestBackgroundImage::handle);
        NETWORK.registerMessage(8, C2SRequestPhotoBackgroundIcon.class, C2SRequestPhotoBackgroundIcon::toBytes, C2SRequestPhotoBackgroundIcon::fromBytes, C2SRequestPhotoBackgroundIcon::handle);
        NETWORK.registerMessage(9, C2SSequencingSynthesizerSelectChange.class, C2SSequencingSynthesizerSelectChange::toBytes, C2SSequencingSynthesizerSelectChange::fromBytes, C2SSequencingSynthesizerSelectChange::handle);
        NETWORK.registerMessage(10, C2SSetTabletBackground.class, C2SSetTabletBackground::toBytes, C2SSetTabletBackground::fromBytes, C2SSetTabletBackground::handle);
        NETWORK.registerMessage(11, C2STabletModuleClicked.class, C2STabletModuleClicked::toBytes, C2STabletModuleClicked::fromBytes, C2STabletModuleClicked::handle);
        NETWORK.registerMessage(12, C2STrackingBeaconData.class, C2STrackingBeaconData::toBytes, C2STrackingBeaconData::fromBytes, C2STrackingBeaconData::handle);
        NETWORK.registerMessage(13, C2STrackingTabletEntryClicked.class, C2STrackingTabletEntryClicked::toBytes, C2STrackingTabletEntryClicked::fromBytes, C2STrackingTabletEntryClicked::handle);
        NETWORK.registerMessage(14, C2SVehicleInputStateUpdated.class, C2SVehicleInputStateUpdated::toBytes, C2SVehicleInputStateUpdated::fromBytes, C2SVehicleInputStateUpdated::handle);
        NETWORK.registerMessage(15, C25StopTrackingTablet.class, C25StopTrackingTablet::toBytes, C25StopTrackingTablet::fromBytes, C25StopTrackingTablet::handle);
        NETWORK.registerMessage(16, CS2UploadPhotoBackgroundImage.class, CS2UploadPhotoBackgroundImage::toBytes, CS2UploadPhotoBackgroundImage::fromBytes, CS2UploadPhotoBackgroundImage::handle);
        NETWORK.registerMessage(17, S2CMachinePositionDirty.class, S2CMachinePositionDirty::toBytes, S2CMachinePositionDirty::fromBytes, S2CMachinePositionDirty::handle);
        NETWORK.registerMessage(18, S2COpenTablet.class, S2COpenTablet::toBytes, S2COpenTablet::fromBytes, S2COpenTablet::handle);
        NETWORK.registerMessage(19, S2COpenTabletModule.class, S2COpenTabletModule::toBytes, S2COpenTabletModule::fromBytes, S2COpenTabletModule::handle);
        NETWORK.registerMessage(20, S2CRegenFenceCache.class, S2CRegenFenceCache::toBytes, S2CRegenFenceCache::fromBytes, S2CRegenFenceCache::handle);
        NETWORK.registerMessage(21, S2CRequestBackgroundIconHeaders.class, S2CRequestBackgroundIconHeaders::toBytes, S2CRequestBackgroundIconHeaders::fromBytes, S2CRequestBackgroundIconHeaders::handle);
        NETWORK.registerMessage(22, S2CSetTrackingDataList.class, S2CSetTrackingDataList::toBytes, S2CSetTrackingDataList::fromBytes, S2CSetTrackingDataList::handle);
        NETWORK.registerMessage(23, S2CStartTrackingTabletHandshake.class, S2CStartTrackingTabletHandshake::toBytes, S2CStartTrackingTabletHandshake::fromBytes, S2CStartTrackingTabletHandshake::handle);
        NETWORK.registerMessage(24, S2CSyncBackgroundIcon.class, S2CSyncBackgroundIcon::toBytes, S2CSyncBackgroundIcon::fromBytes, S2CSyncBackgroundIcon::handle);
        NETWORK.registerMessage(25, S2CSyncBackgroundImage.class, S2CSyncBackgroundImage::toBytes, S2CSyncBackgroundImage::fromBytes, S2CSyncBackgroundImage::handle);
        NETWORK.registerMessage(26, S2CSyncMachineProcesses.class, S2CSyncMachineProcesses::toBytes, S2CSyncMachineProcesses::fromBytes, S2CSyncMachineProcesses::handle);
        NETWORK.registerMessage(27, S2CSyncMachineStack.class, S2CSyncMachineStack::toBytes, S2CSyncMachineStack::fromBytes, S2CSyncMachineStack::handle);
        NETWORK.registerMessage(28, S2CSyncOpenedUsers.class, S2CSyncOpenedUsers::toBytes, S2CSyncOpenedUsers::fromBytes, S2CSyncOpenedUsers::handle);
        NETWORK.registerMessage(29, S2CSyncSequencingSynthesizerSyncSelected.class, S2CSyncSequencingSynthesizerSyncSelected::toBytes, S2CSyncSequencingSynthesizerSyncSelected::fromBytes, S2CSyncSequencingSynthesizerSyncSelected::handle);
        NETWORK.registerMessage(30, S2STrackingTabletUpdateChunk.class, S2STrackingTabletUpdateChunk::toBytes, S2STrackingTabletUpdateChunk::fromBytes, S2STrackingTabletUpdateChunk::handle);
        NETWORK.registerMessage(31, C2SSequencerSynthesizerContainerSlotOpened.class, C2SSequencerSynthesizerContainerSlotOpened::toBytes, C2SSequencerSynthesizerContainerSlotOpened::fromBytes, C2SSequencerSynthesizerContainerSlotOpened::handle);
        NETWORK.registerMessage(32, C2SManualStartRecipe.class, C2SManualStartRecipe::toBytes, C2SManualStartRecipe::fromBytes, C2SManualStartRecipe::handle);
    }

    public static TranslationTextComponent translate(String key, Object... args) {
        return new TranslationTextComponent(MODID + "." + key, args);
    }
}
