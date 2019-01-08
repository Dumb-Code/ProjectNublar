package net.dumbcode.projectnublar.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.Tyrannosaurus;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.dumbcode.projectnublar.server.entity.component.RegisterComponentsEvent;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.*;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.dumbcode.projectnublar.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.utils.JsonHandlers;
import net.dumbcode.projectnublar.server.utils.VoidStorage;
import net.dumbcode.projectnublar.server.world.gen.WorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
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
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber
@Mod(modid = ProjectNublar.MODID, name = ProjectNublar.NAME, version = ProjectNublar.VERSION, dependencies = "required-after:llibrary@[" + ProjectNublar.LLIBRARY_VERSION + ",);required-after:dumblibrary@[" + ProjectNublar.DUMBLIBRARY_VERSION + ",)")
public class ProjectNublar {
    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.0.5";
    public static final String LLIBRARY_VERSION = "1.7.15";
    public static final String DUMBLIBRARY_VERSION = "0.1.9";

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;
    public static IForgeRegistry<EntityComponentType<?>> COMPONENT_REGISTRY;

    @CapabilityInject(EntityManager.class)
    public static final Capability<EntityManager> ENTITY_MANAGER = InjectedUtils.injected();

    private static Logger logger;

    @Mod.Instance(MODID)
    public static ProjectNublar INSTANCE;

    public static SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    public static CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ItemBlock.getItemFromBlock(Blocks.DEADBUSH)); //TODO: custom item
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler.INSTANCE);
        registerPackets();

        GameRegistry.registerWorldGenerator(WorldGenerator.INSTANCE, 0);

        CapabilityManager.INSTANCE.register(EntityManager.class, new VoidStorage<>(), EntityManager.Impl::new);
    }

    public static void spawnParticles(ParticleType type, World world, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int amount, int... data) {
        if(world.isRemote) {
            for (int i = 0; i < amount; i++) {
                spawnParticle0(type, world, xPos, yPos, zPos, xMotion, yMotion, zMotion, data);
            }
        } else {
            //Spawn particles packet
        }
    }

    @SideOnly(Side.CLIENT)
    private static void spawnParticle0(ParticleType type, World world, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int... data) {
        Minecraft.getMinecraft().effectRenderer.addEffect(type.getParticleSupplier().get().createParticle(world, xPos, yPos, zPos, xMotion, yMotion, zMotion, data));
    }

    private void registerPackets() {
        NETWORK.registerMessage(new C0MoveSelectedSkeletalPart.Handler(), C0MoveSelectedSkeletalPart.class, 0, Side.SERVER);
        NETWORK.registerMessage(new S1UpdateSkeletalBuilder.Handler(), S1UpdateSkeletalBuilder.class, 1, Side.CLIENT);
        NETWORK.registerMessage(new C2SkeletalMovement.Handler(), C2SkeletalMovement.class, 2, Side.SERVER);
        NETWORK.registerMessage(new S3HistoryRecord.Handler(), S3HistoryRecord.class, 3, Side.CLIENT);
        NETWORK.registerMessage(new C4MoveInHistory.Handler(), C4MoveInHistory.class, 4, Side.SERVER);
        NETWORK.registerMessage(new S5UpdateHistoryIndex.Handler(), S5UpdateHistoryIndex.class, 5, Side.CLIENT);
        NETWORK.registerMessage(new C6ResetPose.Handler(), C6ResetPose.class, 6, Side.SERVER);
        NETWORK.registerMessage(new S7FullPoseChange.Handler(), S7FullPoseChange.class, 7, Side.CLIENT);
        NETWORK.registerMessage(new C8FullPoseChange.Handler(), C8FullPoseChange.class, 8, Side.SERVER);
        NETWORK.registerMessage(new C9ChangeGlobalRotation.Handler(), C9ChangeGlobalRotation.class, 9, Side.SERVER);
        NETWORK.registerMessage(new S10ChangeGlobalRotation.Handler(), S10ChangeGlobalRotation.class, 10, Side.CLIENT);
        NETWORK.registerMessage(new C11ChangePoleFacing.Handler(), C11ChangePoleFacing.class, 11, Side.SERVER);
        NETWORK.registerMessage(new S12ChangePoleFacing.Handler(), S12ChangePoleFacing.class, 12, Side.CLIENT);
        NETWORK.registerMessage(new C13VehicleInputStateUpdated.Handler(), C13VehicleInputStateUpdated.class, 13, Side.SERVER);
        NETWORK.registerMessage(new C14SequencingSynthesizerSelectChange.Handler(), C14SequencingSynthesizerSelectChange.class, 14, Side.SERVER);
        NETWORK.registerMessage(new S15SyncSequencingSynthesizerSelectChange.Handler(), S15SyncSequencingSynthesizerSelectChange.class, 15, Side.CLIENT);
        NETWORK.registerMessage(new C16DisplayTabbedGui.Handler(), C16DisplayTabbedGui.class, 16, Side.SERVER);
        NETWORK.registerMessage(new S17MachinePositionDirty.Handler(), S17MachinePositionDirty.class, 17, Side.CLIENT);
        NETWORK.registerMessage(new C18OpenContainer.Handler(), C18OpenContainer.class, 18, Side.SERVER);
        NETWORK.registerMessage(new S19SetGuiWindow.Handler(), S19SetGuiWindow.class, 19, Side.CLIENT);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(SkeletalBuilderBlockEntity.class, new ResourceLocation(MODID, "skeletal_builder"));
      
        GameRegistry.registerTileEntity(FossilProcessorBlockEntity.class, new ResourceLocation(MODID, "fossil_processor"));
        GameRegistry.registerTileEntity(DrillExtractorBlockEntity.class, new ResourceLocation(MODID, "drill_extractor"));
        GameRegistry.registerTileEntity(SequencingSynthesizerBlockEntity.class, new ResourceLocation(MODID, "sequencing_synthesizer"));
        GameRegistry.registerTileEntity(EggPrinterBlockEntity.class, new ResourceLocation(MODID, "egg_printer"));
        GameRegistry.registerTileEntity(IncubatorBlockEntity.class, new ResourceLocation(MODID, "incubator"));
        GameRegistry.registerTileEntity(CoalGeneratorBlockEntity.class, new ResourceLocation(MODID, "coal_generator"));
        GameRegistry.registerTileEntity(BlockEntityElectricFencePole.class, new ResourceLocation(MODID, "electric_fence_pole"));
        GameRegistry.registerTileEntity(BlockEntityElectricFence.class, new ResourceLocation(MODID, "electric_fence"));

        for(Map.Entry<Dinosaur, ItemDinosaurMeat> entry : ItemHandler.RAW_MEAT_ITEMS.entrySet()) {
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
            File jsonFile = new File("./mods/projectnublar/debug/" + dino.getRegName().getResourcePath() + ".json");
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
        COMPONENT_REGISTRY = new RegistryBuilder<EntityComponentType<?>>()
                .setType(EntityComponentType.getWildcardType())
                .setName(new ResourceLocation(ProjectNublar.MODID, "component"))
                .create();
        MinecraftForge.EVENT_BUS.post(new RegisterComponentsEvent(COMPONENT_REGISTRY));

        DINOSAUR_REGISTRY = new RegistryBuilder<Dinosaur>()
                .setType(Dinosaur.class)
                .setName(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "missing"))
                .set(((key, isNetwork) -> Dinosaur.MISSING))
                .create();
        registerJsonDinosaurs();
        MinecraftForge.EVENT_BUS.post(new RegisterDinosaurEvent(DINOSAUR_REGISTRY));
    }

    private static void registerJsonDinosaurs() {
        GsonBuilder builder = new GsonBuilder();
        JsonHandlers.registerAllHandlers(builder);
        Gson gson = builder.create();
        JsonUtil.registerModJsons(DINOSAUR_REGISTRY, gson, ProjectNublar.MODID, "dinosaurs");
    }

    @SubscribeEvent
    public static void register(RegisterDinosaurEvent event) {
        event.getRegistry().register(new Tyrannosaurus().setRegistryName("projectnublar:missing")); // TODO: custom class?
        event.getRegistry().register(new Tyrannosaurus().setRegistryName("projectnublar:tyrannosaurus"));
    }

    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandProjectNublar());
    }
}
