package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.animation.objects.RenderAnimatableCube;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.*;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public enum MultipartSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private AnimationComponent[] animations = new AnimationComponent[0];
    private MultipartEntityComponent[] multiparts = new MultipartEntityComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.MULTIPART, EntityComponentTypes.ANIMATION);
        this.animations = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animations);
        this.multiparts = family.populateBuffer(EntityComponentTypes.MULTIPART, this.multiparts);
        this.entities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.multiparts.length; i++) {
            updatePart(this.entities[i], this.multiparts[i], this.animations[i]);
        }
    }


    private static void updatePart(Entity entity, MultipartEntityComponent multipart, AnimationComponent animation) {
        if(animation.animationWrapper == null) {
            return;
        }

        List<AnimationLayer<DinosaurEntity, ModelStage>> layers = animation.animationWrapper.getLayers();
        for (AnimationLayer<DinosaurEntity, ModelStage> layer : layers) {
            for (String cubeName : layer.getCubeNames()) {
                layer.getAnicubeRef().apply(cubeName).reset();
            }
        }
        for (AnimationLayer<DinosaurEntity, ModelStage> layer : layers) {
            layer.animate(entity.ticksExisted);
        }
        Matrix4d entityRotate = new Matrix4d();
        entityRotate.rotY(-Math.toRadians(entity.rotationYaw));

        Function<String, AnimationLayer.AnimatableCube> function = layers.get(0).getAnicubeRef(); //ew
        for (MultipartEntityComponent.LinkedEntity cube : multipart.entities) {
            Entity cubeEntity = cube.getEntity();
            cubeEntity.onUpdate();
            AnimationLayer.AnimatableCube animatableCube = function.apply(cube.getCubeName());
            if (animatableCube != null) {
                Vec3d partOrigin = animatableCube.getModelPos(0.5F, 0.5F, 0.5F);
                Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y + 1.5, partOrigin.z);
                rendererPos.scale(2.5);
                entityRotate.transform(rendererPos);
                cubeEntity.setPosition(rendererPos.x + entity.posX, rendererPos.y + entity.posY - cubeEntity.height/2, rendererPos.z + entity.posZ);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ClientTickEvent event) {
        World world = Minecraft.getMinecraft().world;
        if(world != null) {
            for (Entity entity : world.loadedEntityList) {
                if(entity instanceof EntityPart && (((EntityPart) entity).getParent() == null || ((EntityPart) entity).getParent().isDead)) {
                    entity.setDead();
                }
                if(entity instanceof ComponentAccess) {
                    AnimationComponent animation = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.ANIMATION);
                    MultipartEntityComponent multipart = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.MULTIPART);
                    if(animation != null && multipart != null) {
                        updatePart(entity, multipart, animation);
                    }
                }
            }
        }
    }
}
