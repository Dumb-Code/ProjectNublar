package net.dumbcode.projectnublar.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dumbcode.dumblibrary.client.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.entity.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.entity.system.impl.HerdSystem;
import net.dumbcode.dumblibrary.server.entity.system.impl.MetabolismSystem;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.projectnublar.server.block.BlockCreativePowerSource;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DataSerializerHandler;
import net.dumbcode.projectnublar.server.entity.system.impl.AgeSystem;
import net.dumbcode.projectnublar.server.entity.system.impl.MultipartSystem;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.registry.RegisterDinosaurEvent;
import net.dumbcode.projectnublar.server.registry.RegisterPlantEvent;
import net.dumbcode.projectnublar.server.utils.JsonHandlers;
import net.dumbcode.projectnublar.server.utils.VoidStorage;
import net.dumbcode.projectnublar.server.world.gen.WorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
import net.minecraftforge.registries.ObjectHolderRegistry;
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
    public static final String VERSION = "0.0.22";
    public static final String DUMBLIBRARY_VERSION = "0.2.4";

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;
    public static IForgeRegistry<Plant> PLANT_REGISTRY;

    private static Logger logger;

    @Mod.Instance(MODID)
    public static ProjectNublar INSTANCE;

    public static SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        ObjectHolderRegistry.INSTANCE.applyObjectHolders(); //Not sure about this. It seems costly. But its needed to make sure the components are injected
        MinecraftForge.EVENT_BUS.post(new RegisterDinosaurEvent(DINOSAUR_REGISTRY));
        MinecraftForge.EVENT_BUS.post(new RegisterPlantEvent(PLANT_REGISTRY));

        registerJsonDinosaurs();

        DINOSAUR_REGISTRY.forEach(Dinosaur::attachDefaultComponents);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler.INSTANCE);
        registerPackets();

        GameRegistry.registerWorldGenerator(WorldGenerator.INSTANCE, 0);

        CapabilityManager.INSTANCE.register(EntityManager.class, new VoidStorage<>(), EntityManager.Impl::new);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        DataSerializerHandler.register();

        for (Dinosaur dinosaur : DINOSAUR_REGISTRY.getValuesCollection()) {
            ResourceLocation regName = dinosaur.getRegName();
            for (AgeStage orderedAge : dinosaur.getAttacher().getStorage(ComponentHandler.AGE).getOrderedAges()) {
                Map<String, AnimationContainer> container = dinosaur.getModelContainer();
                container.put(orderedAge.getName(), new AnimationContainer(new ResourceLocation(regName.getNamespace(), regName.getPath() + "_" + orderedAge.getName())));

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
                e.printStackTrace();
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
    }

    @SubscribeEvent
    public static void register(RegisterSystemsEvent event) {
        event.registerSystem(AgeSystem.INSTANCE);
        event.registerSystem(MultipartSystem.INSTANCE);
        event.registerSystem(MetabolismSystem.INSTANCE);
        event.registerSystem(HerdSystem.INSTANCE);
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
        NETWORK.registerMessage(new C0MoveSelectedSkeletalPart.Handler(), C0MoveSelectedSkeletalPart.class, 0, Side.SERVER);
        NETWORK.registerMessage(new S1UpdateSkeletalBuilder.Handler(), S1UpdateSkeletalBuilder.class, 1, Side.CLIENT);
        NETWORK.registerMessage(new C2SkeletalMovement.Handler(), C2SkeletalMovement.class, 2, Side.SERVER);
        NETWORK.registerMessage(new S3HistoryRecord.Handler(), S3HistoryRecord.class, 3, Side.CLIENT);
        NETWORK.registerMessage(new C4MoveInHistory.Handler(), C4MoveInHistory.class, 4, Side.SERVER);
        NETWORK.registerMessage(new S5UpdateHistoryIndex.Handler(), S5UpdateHistoryIndex.class, 5, Side.CLIENT);
        NETWORK.registerMessage(new S7FullPoseChange.Handler(), S7FullPoseChange.class, 7, Side.CLIENT);
        NETWORK.registerMessage(new C8FullPoseChange.Handler(), C8FullPoseChange.class, 8, Side.SERVER);
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
    }
}
