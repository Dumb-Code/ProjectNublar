package net.dumbcode.projectnublar.server;

import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurRenderer;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.Velociraptor;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.dumbcode.projectnublar.server.item.NublarItems;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
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

    public static CreativeTabs TAB = new CreativeTabs("projectnublar") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ItemBlock.getItemFromBlock(Blocks.DEADBUSH)); // TODO: custom item
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, DinosaurRenderer::new);
        createItems();
    }

    private void createItems() {
        NublarItems.DINOSAUR_MEAT = (ItemDinosaurMeat)(new ItemDinosaurMeat()
                .setRegistryName(new ResourceLocation(MODID, "dinosaur_meat"))
                .setUnlocalizedName("dinosaur_meat")
                .setCreativeTab(TAB));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @SubscribeEvent
    public static void createRegisteries(RegistryEvent.NewRegistry event) {
        DINOSAUR_REGISTRY = new RegistryBuilder<Dinosaur>()
                .setType(Dinosaur.class)
                .setName(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                .setDefaultKey(new ResourceLocation(ProjectNublar.MODID,"velociraptor"))
                .set(((key, isNetwork) -> Dinosaur.MISSING))
                .add((owner, stage, id, obj, oldObj) -> System.out.println(obj))
                .create();
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Dinosaur> event) {
        event.getRegistry().register(new Velociraptor().setRegistryName("projectnublar:velociraptor"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(NublarItems.getAllItems());
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Dinosaur> event) {
        Dinosaur velociraptor = new Dinosaur().setRegistryName("projectnublar:velociraptor");
        velociraptor.setCookedMeatHealAmount(10);
        velociraptor.setCookedMeatSaturation(1f);
        velociraptor.setRawMeatHealAmount(4);
        velociraptor.setRawMeatSaturation(0.6f);
        event.getRegistry().register(velociraptor);

        NublarItems.DINOSAUR_MEAT.registerOreNames();
    }

    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandProjectNublar());
    }
}
