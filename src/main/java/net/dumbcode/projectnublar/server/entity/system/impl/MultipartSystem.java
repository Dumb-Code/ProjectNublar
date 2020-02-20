package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.util.Objects;
import java.util.function.Function;

public class MultipartSystem implements EntitySystem {

    private Entity[] entities = new Entity[0];
    private AnimationComponent[] animations = new AnimationComponent[0];
    private MultipartEntityComponent[] multiparts = new MultipartEntityComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.MULTIPART, EntityComponentTypes.ANIMATION);
        this.animations = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animations);
        this.multiparts = family.populateBuffer(ComponentHandler.MULTIPART, this.multiparts);
        this.entities = family.getEntities();
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.multiparts.length; i++) {
            updatePart(this.entities[i], this.multiparts[i], this.animations[i]);
        }
    }

    private void updatePart(Entity entity, MultipartEntityComponent multipart, AnimationComponent animation) {
        if(entity.ticksExisted % 3 != 0) {
            return;
        }

        AnimationLayer layer = animation.getAnimationLayer(entity);
        if(layer == null) {
            return;
        }

        DinosaurEntity dinosaur = null;
        if(entity instanceof DinosaurEntity) {
            dinosaur = (DinosaurEntity) entity;
        }

        if(!entity.world.isRemote) {
            for (String cubeName : layer.getCubeNames()) {
                layer.getAnicubeRef().apply(cubeName).reset();
            }
            layer.animate(entity.ticksExisted);
        }

        Matrix4d entityRotate = new Matrix4d();
        entityRotate.rotY(-Math.toRadians(entity.rotationYaw));

        Function<String, AnimationLayer.AnimatableCube> function = layer.getAnicubeRef();
        for (MultipartEntityComponent.LinkedEntity cube : multipart.getEntities()) {
            for (Entity e : entity.world.loadedEntityList) {
                if(e instanceof EntityPart && e.getUniqueID().equals(cube.getEntityUUID())) {
                    AnimationLayer.AnimatableCube animatableCube = function.apply(cube.getCubeName());
                    if (animatableCube != null && e.ticksExisted > 1) {
                        EntityPart cubeEntity = (EntityPart) e;
                        Point3d sp = null;
                        Point3d ep = null;

                        double minX = Integer.MAX_VALUE;
                        double minY = Integer.MAX_VALUE;
                        double minZ = Integer.MAX_VALUE;

                        double maxX = Integer.MIN_VALUE;
                        double maxY = Integer.MIN_VALUE;
                        double maxZ = Integer.MIN_VALUE;

                        for (int i = 0; i < 8; i++) {
                            Vec3d startPoint = TabulaUtils.getModelPosAlpha(animatableCube, (i >> 2)&1, (i >> 1)&1, i&1);
                            Point3d point = new Point3d(startPoint.x, startPoint.y + 1.5, startPoint.z);

                            if(dinosaur != null) {
                                dinosaur.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> {
                                            point.x *= floats[0];
                                            point.y *= floats[1];
                                            point.z *= floats[2];
                                        }
                                );
                            }
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


                        Vec3d size = new Vec3d(maxX, maxY, maxZ).subtract(minX, minY, minZ).add(0.1F, 0.1F, 0.1F); //0.1F -> padding

                        cubeEntity.setSize(size);

                        Objects.requireNonNull(ep);
                        Objects.requireNonNull(sp);

                        cubeEntity.setPosition(sp.x + (ep.x - sp.x) / 2D + entity.posX, sp.y + (ep.y - sp.y) / 2D + entity.posY - size.y / 2, sp.z + (ep.z - sp.z) / 2D + entity.posZ);
                    }

                    break;
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        World world = Minecraft.getMinecraft().world;
        if(world != null && !Minecraft.getMinecraft().isGamePaused()) {
            for (Entity entity : world.loadedEntityList) {
                if(entity instanceof ComponentAccess) {
                    AnimationComponent animation = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.ANIMATION);
                    MultipartEntityComponent multipart = ((ComponentAccess) entity).getOrNull(ComponentHandler.MULTIPART);
                    if(animation != null && multipart != null) {
                        updatePart(entity, multipart, animation);
                    }
                }
            }
        }
    }
}
