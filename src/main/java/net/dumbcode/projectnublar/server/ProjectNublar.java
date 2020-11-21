package net.dumbcode.projectnublar.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.dumbcode.projectnublar.client.gui.icons.EnumWeatherIcons;
import net.dumbcode.projectnublar.server.animation.AnimationFactorHandler;
import net.dumbcode.projectnublar.server.block.BlockCreativePowerSource;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DataSerializerHandler;
import net.dumbcode.projectnublar.server.entity.system.impl.*;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.registry.RegisterDinosaurEvent;
import net.dumbcode.projectnublar.server.registry.RegisterPlantEvent;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.utils.JsonHandlers;
import net.dumbcode.projectnublar.server.world.gen.WorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
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
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber
@Mod(modid = ProjectNublar.MODID, name = ProjectNublar.NAME, version = ProjectNublar.VERSION, dependencies = "required-after:dumblibrary")
public class ProjectNublar {


    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.1.25";
    public static final String DUMBLIBRARY_VERSION = "0.2.4";

    public static final boolean DEBUG = false;

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;
    public static IForgeRegistry<Plant> PLANT_REGISTRY;

    public static IForgeRegistry<TabletModuleType<?>> TABLET_MODULES_REGISTRY;

    private static Logger logger;

    @Mod.Instance(MODID)
    public static ProjectNublar INSTANCE;

    public static final  SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        MinecraftForge.EVENT_BUS.post(new RegisterDinosaurEvent(DINOSAUR_REGISTRY));
        MinecraftForge.EVENT_BUS.post(new RegisterPlantEvent(PLANT_REGISTRY));

        registerJsonDinosaurs();

        DINOSAUR_REGISTRY.forEach(Dinosaur::attachDefaultComponents);
        PLANT_REGISTRY.forEach(Plant::attachComponents);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler.INSTANCE);
        registerPackets();

        GameRegistry.registerWorldGenerator(WorldGenerator.INSTANCE, 0);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        DataSerializerHandler.register();
        if(SidedExecutor.CLIENT) {
            EnumWeatherIcons.register();
            TabletBackground.registerDefaults();
        }
        AnimationFactorHandler.register();

        for (Dinosaur dinosaur : DINOSAUR_REGISTRY.getValuesCollection()) {
            ResourceLocation regName = dinosaur.getRegName();
            for (AgeStage orderedAge : dinosaur.getAttacher().getStorage(ComponentHandler.AGE).getOrderedAges()) {
                Map<String, AnimationContainer> container = dinosaur.getModelContainer();

                container.computeIfAbsent(orderedAge.getModelStage(),
                    s -> new AnimationContainer(new ResourceLocation(regName.getNamespace(), regName.getPath() + "/" + s)));

            }
        }

        GameRegistry.registerTileEntity(SkeletalBuilderBlockEntity.class, new ResourceLocation(MODID, "skeletal_builder"));
        GameRegistry.registerTileEntity(FossilProcessorBlockEntity.class, new ResourceLocation(MODID, "fossil_processor"));
        GameRegistry.registerTileEntity(DrillExtractorBlockEntity.class, new ResourceLocation(MODID, "drill_extractor"));
        GameRegistry.registerTileEntity(SequencingSynthesizerBlockEntity.class, new ResourceLocation(MODID, "sequencing_synthesizer"));
        GameRegistry.registerTileEntity(EggPrinterBlockEntity.class, new ResourceLocation(MODID, "egg_printer"));
        GameRegistry.registerTileEntity(IncubatorBlockEntity.class, new ResourceLocation(MODID, "incubator"));
        GameRegistry.registerTileEntity(CoalGeneratorBlockEntity.class, new ResourceLocation(MODID, "coal_generator"));
        GameRegistry.registerTileEntity(BlockEntityElectricFencePole.class, new ResourceLocation(MODID, "electric_fence_pole"));
        GameRegistry.registerTileEntity(BlockEntityElectricFence.class, new ResourceLocation(MODID, "electric_fence"));
        GameRegistry.registerTileEntity(BlockCreativePowerSource.TileEntityCreativePowerSource.class, new ResourceLocation(MODID, "creative_power"));
        GameRegistry.registerTileEntity(TrackingBeaconBlockEntity.class, new ResourceLocation(MODID, "tracking_beacon"));
        GameRegistry.registerTileEntity(PylonHeadBlockEntity.class, new ResourceLocation(MODID, "pylon_head"));

        for (Map.Entry<Dinosaur, ItemDinosaurMeat> entry : ItemHandler.RAW_MEAT_ITEMS.entrySet()) {
            Dinosaur dino = entry.getKey();
            ItemDinosaurMeat referenceRawMeat = entry.getValue();
            ItemDinosaurMeat referenceCookedMeat = ItemHandler.COOKED_MEAT_ITEMS.get(dino);
            if (referenceCookedMeat == null) {
                continue;
            }

            // handle all items linked to the most specific name
            NonNullList<ItemStack> rawMeats = OreDictionary.getOres(referenceRawMeat.getMostSpecificOreName());
            for (ItemStack rawMeat : rawMeats) {
                FurnaceRecipes.instance().addSmeltingRecipe(rawMeat, new ItemStack(referenceCookedMeat), dino.getItemProperties().getCookingExperience());
            }
        }

        // TODO: Remove, debug only
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        DINOSAUR_REGISTRY.getValuesCollection().forEach(dino -> {
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

        TABLET_MODULES_REGISTRY = new RegistryBuilder<TabletModuleType<?>>()
                .setType(TabletModuleType.getWildcardType())
                .setName(new ResourceLocation(MODID, "module_type"))
                .create();
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

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandProjectNublar());
    }

    public static void spawnParticles(ParticleType type, World world, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int amount, int... data) {
        if (world.isRemote) {
            for (int i = 0; i < amount; i++) {
                spawnParticle0(type, world, xPos, yPos, zPos, xMotion, yMotion, zMotion, data);
            }
        } else {
            NETWORK.sendToDimension(new S21SpawnParticle(type, xPos, yPos, zPos, xMotion, yMotion, zMotion, amount, data), world.provider.getDimension());
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    @SideOnly(Side.CLIENT)
    private static void spawnParticle0(ParticleType type, World world, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int... data) {
        Minecraft.getMinecraft().effectRenderer.addEffect(type.getParticleSupplier().get().createParticle(world, xPos, yPos, zPos, xMotion, yMotion, zMotion, data));
    }

    private static void registerJsonDinosaurs() {
        GsonBuilder builder = new GsonBuilder();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        JsonUtil.registerModJsons(DINOSAUR_REGISTRY, gson, ProjectNublar.MODID, "dinosaurs");
    }

    private void registerPackets() {

        //Now you might be asking where the packets 0-8 are, they simply got moved to dumb lib and i don't want to change the packet names
        NETWORK.registerMessage(new C9ChangeGlobalRotation.Handler(), C9ChangeGlobalRotation.class, 9, Side.SERVER);
        NETWORK.registerMessage(new S10ChangeGlobalRotation.Handler(), S10ChangeGlobalRotation.class, 10, Side.CLIENT);
        NETWORK.registerMessage(new C11UpdatePoleList.Handler(), C11UpdatePoleList.class, 11, Side.SERVER);
        NETWORK.registerMessage(new S12UpdatePoleList.Handler(), S12UpdatePoleList.class, 12, Side.CLIENT);
        NETWORK.registerMessage(new C13VehicleInputStateUpdated.Handler(), C13VehicleInputStateUpdated.class, 13, Side.SERVER);
        NETWORK.registerMessage(new C14SequencingSynthesizerSelectChange.Handler(), C14SequencingSynthesizerSelectChange.class, 14, Side.SERVER);
        NETWORK.registerMessage(new S15SyncSequencingSynthesizerSelectChange.Handler(), S15SyncSequencingSynthesizerSelectChange.class, 15, Side.CLIENT);
        NETWORK.registerMessage(new C16DisplayTabbedGui.Handler(), C16DisplayTabbedGui.class, 16, Side.SERVER);
        NETWORK.registerMessage(new S17MachinePositionDirty.Handler(), S17MachinePositionDirty.class, 17, Side.CLIENT);
        NETWORK.registerMessage(new C18OpenContainer.Handler(), C18OpenContainer.class, 18, Side.SERVER);
        NETWORK.registerMessage(new S19SetGuiWindow.Handler(), S19SetGuiWindow.class, 19, Side.CLIENT);
        NETWORK.registerMessage(new S20RegenCache.Handler(), S20RegenCache.class, 20, Side.CLIENT);
        NETWORK.registerMessage(new S21SpawnParticle.Handler(), S21SpawnParticle.class, 21, Side.CLIENT);
        NETWORK.registerMessage(new S22StartTrackingTabletHandshake.Handler(), S22StartTrackingTabletHandshake.class, 22, Side.CLIENT);
        NETWORK.registerMessage(new C23ConfirmTrackingTablet.Handler(), C23ConfirmTrackingTablet.class, 23, Side.SERVER);
        NETWORK.registerMessage(new S24TrackingTabletUpdateChunk.Handler(), S24TrackingTabletUpdateChunk.class, 24, Side.CLIENT);
        NETWORK.registerMessage(new C25StopTrackingTablet.Handler(), C25StopTrackingTablet.class, 25, Side.SERVER);
        NETWORK.registerMessage(new S26OpenTablet.Handler(), S26OpenTablet.class, 26, Side.CLIENT);
        NETWORK.registerMessage(new C27InstallModule.Handler(), C27InstallModule.class, 27, Side.SERVER);
        NETWORK.registerMessage(new C28LinkClicked.Handler(), C28LinkClicked.class, 28, Side.SERVER);
        NETWORK.registerMessage(new S29OpenTabletModule.Handler(), S29OpenTabletModule.class, 29, Side.CLIENT);
        NETWORK.registerMessage(new C30TrackingTabletEntryClicked.Handler(), C30TrackingTabletEntryClicked.class, 30, Side.SERVER);
        NETWORK.registerMessage(new C31TrackingBeaconDataChanged.Handler(), C31TrackingBeaconDataChanged.class, 31, Side.SERVER);
        NETWORK.registerMessage(new S32SetTrackingDataList.Handler(), S32SetTrackingDataList.class, 32, Side.CLIENT);
        NETWORK.registerMessage(new C33SetTabletBackground.Handler(), C33SetTabletBackground.class, 33, Side.SERVER);
        NETWORK.registerMessage(new C34UploadImage.Handler(), C34UploadImage.class, 34, Side.SERVER);
        NETWORK.registerMessage(new C35RequestAllIcons.Handler(), C35RequestAllIcons.class, 35, Side.SERVER);
        NETWORK.registerMessage(new S36RequestBackgroundIconHeaders.Handler(), S36RequestBackgroundIconHeaders.class, 36, Side.CLIENT);
        NETWORK.registerMessage(new C37RequestImageBackground.Handler(), C37RequestImageBackground.class, 37, Side.SERVER);
        NETWORK.registerMessage(new S38SyncBackgroundImage.Handler(), S38SyncBackgroundImage.class, 38, Side.CLIENT);
        NETWORK.registerMessage(new S39SyncBackgroundIcon.Handler(), S39SyncBackgroundIcon.class, 39, Side.CLIENT);
        NETWORK.registerMessage(new C40RequestBackgroundIcon.Handler(), C40RequestBackgroundIcon.class, 40, Side.SERVER);
        NETWORK.registerMessage(new C41PlaceIncubatorEgg.Handler(), C41PlaceIncubatorEgg.class, 41, Side.SERVER);
        NETWORK.registerMessage(new S42SyncMachineProcesses.Handler(), S42SyncMachineProcesses.class, 42, Side.CLIENT);
        NETWORK.registerMessage(new S43SyncMachineStack.Handler(), S43SyncMachineStack.class, 43, Side.CLIENT);
        NETWORK.registerMessage(new S44SyncOpenedUsers.Handler(), S44SyncOpenedUsers.class, 44, Side.CLIENT);
    }
}
