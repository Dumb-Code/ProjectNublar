package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.*;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.util.List;
import java.util.Objects;
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
        long l = System.nanoTime();

        Function<String, AnimationLayer.AnimatableCube> function = layers.get(0).getAnicubeRef(); //ew
        for (MultipartEntityComponent.LinkedEntity cube : multipart.entities) {
            Entity cubeEntity = cube.getEntity();
            AnimationLayer.AnimatableCube animatableCube = function.apply(cube.getCubeName());
            if (animatableCube != null) {
                Point3d sp = null;
                Point3d ep = null;

                double minX = Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double minZ = Double.MAX_VALUE;

                double maxX = Double.MIN_VALUE;
                double maxY = Double.MIN_VALUE;
                double maxZ = Double.MIN_VALUE;

                for (int i = 0; i < 8; i++) {
                    Vec3d startPoint = animatableCube.getModelPos((i >> 2)&1, (i >> 1)&1, i&1);
                    Point3d point = new Point3d(startPoint.x, startPoint.y + 1.5, startPoint.z);
                    point.scale(2.5);
                    entityRotate.transform(point);

                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                    minZ = Math.min(minZ, point.z);

                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    maxZ = Math.max(maxZ, point.z);

                    if(i == 0) {
                        sp = point;
                    } else if(i == 7) {
                        ep = point;
                    }
                }



                if(cubeEntity instanceof EntityPart) { //interface/ecs ?
                    ((EntityPart) cubeEntity).cubeWidth = maxX - minX + 0.25F;
                    ((EntityPart) cubeEntity).cubeHeight = maxY - minY + 0.25F;
                    ((EntityPart) cubeEntity).cubeDepth = maxZ - minZ + 0.25F;
                }

                Objects.requireNonNull(ep);
                Objects.requireNonNull(sp);

                cubeEntity.setLocationAndAngles(sp.x + (ep.x - sp.x) / 2D + entity.posX, sp.y + (ep.y - sp.y) / 2D + entity.posY - cubeEntity.height/2, sp.z + (ep.z - sp.z) / 2D + entity.posZ, 0, 0);

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
