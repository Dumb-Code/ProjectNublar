package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.studio.animation.instance.ModelAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

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
        ModelAnimationHandler handler = animation.getAnimationHandler();

        DinosaurEntity dinosaur = null;
        if(entity instanceof DinosaurEntity) {
            dinosaur = (DinosaurEntity) entity;
        }

        if(!entity.level.isClientSide && entity.level.getGameTime() % 5 == 0) {
            handler.animate(animation.getModelCubes(), 1 / 4F);
        }

        if(entity.tickCount % 3 != 0) {
            return;
        }

        Matrix3f entityRotate = new Matrix3f();
        entityRotate.setIdentity();
        entityRotate.mul(Vector3f.YP.rotationDegrees(-entity.yRot));

        for (AnimatedReferenceCube modelCube : animation.getModelCubes()) {
            for (MultipartEntityComponent.LinkedEntity cube : multipart.getEntities()) {
                if(!cube.getCubeName().equals(modelCube.getInfo().getName())) {
                    continue;
                }
                EntityPart part = cube.getCachedPart(entity.level);
                if(part == null) {
                    continue;
                }

                if (part.tickCount > 1) {
                    Vector3f sp = null;
                    Vector3f ep = null;

                    double minX = Integer.MAX_VALUE;
                    double minY = Integer.MAX_VALUE;
                    double minZ = Integer.MAX_VALUE;

                    double maxX = Integer.MIN_VALUE;
                    double maxY = Integer.MIN_VALUE;
                    double maxZ = Integer.MIN_VALUE;

                    for (int i = 0; i < 8; i++) {
                        Vector3f startPoint = DCMUtils.getModelPosAlpha(modelCube, (i >> 2) & 1, (i >> 1) & 1, i & 1);
                        Vector3f point = new Vector3f(startPoint.x(), -startPoint.y() + 1.5F, -startPoint.z());

                        if (dinosaur != null) {
                            dinosaur.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> point.mul(floats[0], floats[1], floats[2]));
                        }

                        point.transform(entityRotate);

                        minX = Math.min(minX, point.x());
                        minY = Math.min(minY, point.y());
                        minZ = Math.min(minZ, point.z());

                        maxX = Math.max(maxX, point.x());
                        maxY = Math.max(maxY, point.y());
                        maxZ = Math.max(maxZ, point.z());

                        if (i == 0) {
                            sp = point;
                        } else if (i == 7) {
                            ep = point;
                        }
                    }


                    Vector3d size = new Vector3d(maxX, maxY, maxZ).subtract(minX, minY, minZ).add(0.1F, 0.1F, 0.1F); //0.1F -> padding

                    part.setSize(size);

                    Objects.requireNonNull(ep);
                    Objects.requireNonNull(sp);

                    Vector3d position = entity.position();
                    part.setPos(
                        sp.x() + (ep.x() - sp.x()) / 2D + position.x,
                        sp.y() + (ep.y() - sp.y()) / 2D + position.y - size.y / 2F,
                        sp.z() + (ep.z() - sp.z()) / 2D + position.z
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        ClientWorld level = Minecraft.getInstance().level;
        if(level != null && !Minecraft.getInstance().isPaused()) {
            for (Entity entity : level.entitiesForRendering()) {
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
