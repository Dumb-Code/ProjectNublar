package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SpeedTrackingComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DinosaurMovementSystem implements EntitySystem {

    private static final int MOVEMENT_CHANNEL = 60;
    private static final int BREATHING_CHANNEL = 69;

    private Entity[] entities = new Entity[0];
    private SpeedTrackingComponent[] speedTrackingComponents = new SpeedTrackingComponent[0];
    private AnimationComponent[] components = new AnimationComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.ANIMATION);
        this.entities = family.getEntities();
        this.speedTrackingComponents = family.populateBuffer(EntityComponentTypes.SPEED_TRACKING, this.speedTrackingComponents);
        this.components = family.populateBuffer(EntityComponentTypes.ANIMATION, this.components);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            AnimationComponent component = this.components[i];
            SpeedTrackingComponent speedTrackingComponent = this.speedTrackingComponents[i];
            this.ensureAnimationPlaying(entity, component, speedTrackingComponent);
        }
    }

    private void ensureAnimationPlaying(Entity entity, AnimationComponent component, SpeedTrackingComponent comp) {

        Vector3d movement = entity.getDeltaMovement();
        double movementAmount = movement.distanceToSqr(0, movement.y, 0);
        Animation landAnimation = movementAmount > 0.03 ? AnimationHandler.RUNNING : AnimationHandler.WALKING;
        Animation animation = entity.isInWater() ? AnimationHandler.SWIMMING : landAnimation;
        if(!component.isChannelActive(MOVEMENT_CHANNEL)) {
            component.playAnimation(animation, MOVEMENT_CHANNEL)
                .withSpeed(() -> {
                    Vector3d movement1 = entity.getDeltaMovement();
                    double movementAmount1 = Math.sqrt(movement1.distanceToSqr(0, movement1.y, 0));
                    float a = (float) (movementAmount1 * 5F);
                    return a;
                })
                .withDegreeFactor(() -> {
                    float a = Math.min((float) (comp.getPreviousSpeed()) * 11F, 1.0F);
                    return 1F;
                })
                .loopForever();
//                .withDegreeFactor(AnimationFactorHandler.LIMB_SWING)
//                .withSpeedFactor(AnimationFactorHandler.LIMB_SWING),;
        }
//        else {
//            //TODO-stream: YIKES, Don't do this, (or maybe do)
//            component.forceAnimationInfo(MOVEMENT_CHANNEL, animation);
//        }

        if(!component.isChannelActive(BREATHING_CHANNEL)) {
            component.playAnimation(AnimationHandler.BREATHING, BREATHING_CHANNEL).loopForever();
        }
    }

    @SubscribeEvent
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        ClientWorld world = Minecraft.getInstance().level;
        if(world != null && !Minecraft.getInstance().isPaused()) {
            for (Entity entity : world.entitiesForRendering()) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(a -> ((ComponentAccess) entity).get(EntityComponentTypes.SPEED_TRACKING).ifPresent(s ->
                            this.ensureAnimationPlaying(entity, a, s)
                        ));
                }
            }
        }
    }
}
