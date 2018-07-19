package net.dumbcode.projectnublar.server;

import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurRenderer;
import net.dumbcode.projectnublar.client.render.dinosaur.PoseHandler;
import net.dumbcode.projectnublar.client.render.dinosaur.TabulaUtils;
import net.dumbcode.projectnublar.client.render.dinosaur.objects.EntityAnimator;
import net.dumbcode.projectnublar.server.command.CommandProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
@Mod(modid = ProjectNublar.MODID, name = ProjectNublar.NAME, version = ProjectNublar.VERSION)
public class ProjectNublar
{
    public static final String MODID = "projectnublar";
    public static final String NAME = "Project Nublar";
    public static final String VERSION = "0.0.1";

    public static IForgeRegistry<Dinosaur> DINOSAUR_REGISTRY;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, DinosaurRenderer::new);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for (Dinosaur dinosaur : DINOSAUR_REGISTRY.getValuesCollection()) {
            dinosaur.setPoseHandler(new PoseHandler(dinosaur));
            dinosaur.setModel(new TabulaModel(TabulaUtils.getModelContainer(new ResourceLocation(ProjectNublar.MODID, "models/entities/velociraptor/adult/velociraptor_adult_idle")), new EntityAnimator()));
        }
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

//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<Dinosaur> event) {
//        event.getRegistry().register(new Dinosaur().setRegistryName("projectnublar:velociraptor"));
//    }
    
    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandProjectNublar());
    }
}
