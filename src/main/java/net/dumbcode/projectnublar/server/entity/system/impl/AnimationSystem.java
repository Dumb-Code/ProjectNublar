package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public enum AnimationSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private AnimationComponent[] animations = new AnimationComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.ANIMATION);
        this.entities = family.getEntities();
        this.animations = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update() {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            AnimationComponent animation = this.animations[i];
            if(!entity.world.isRemote) { //Server side only. Client side is already handled
                if(animation.animationWrapper == null) {
                    animation.createServersideWrapper(entity);
                }
            }
        }
    }
}
