package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.animation.AnimationFactorHandler;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DinosaurMovementSystem implements EntitySystem {

    private static final int MOVEMENT_CHANNEL = 60;

    private Entity[] entities = new Entity[0];
    private AnimationComponent<?>[] components = new AnimationComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.ANIMATION);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(EntityComponentTypes.ANIMATION, this.components);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            AnimationComponent<?> component = this.components[i];
            this.ensureAnimationPlaying(entity, component);
        }
    }

    private void ensureAnimationPlaying(Entity entity, AnimationComponent<?> component) {
        if(component.isReadyForAnimations()) {
            Animation landAnimation = (entity.motionX*entity.motionX + entity.motionZ*entity.motionZ) > 0.03 ? AnimationHandler.RUNNING : AnimationHandler.WALKING;
            Animation animation = entity.isInWater() ? AnimationHandler.SWIMMING : landAnimation;
            if(!component.isChannelActive(MOVEMENT_CHANNEL)) {
                component.playAnimation((ComponentAccess) entity,
                    animation.createEntry().loop()
                        .withDegreeFactor(AnimationFactorHandler.LIMB_SWING)
                        .withSpeedFactor(AnimationFactorHandler.LIMB_SWING),
                    MOVEMENT_CHANNEL
                );
            } else {
                AnimationWrap wrap = component.getWrap(MOVEMENT_CHANNEL);
                if(wrap != null) {
                    //TODO-stream: YIKES, Don't do this, (or maybe do)
                    wrap.getEntry().setAnimation(animation);
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
                    ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(a -> this.ensureAnimationPlaying(entity, a));
                }
            }
        }
    }
}
