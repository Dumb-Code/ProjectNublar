package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

public class AnimationComponent implements EntityComponent {

    public Animation<ModelStage> animation = EnumAnimation.IDLE.get();

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound; //TODO: serialize the animation ?
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
    }

    public void setAnimation(DinosaurEntity entity, Animation<ModelStage> animation) {
        this.animation = animation;
        if(!entity.world.isRemote) {
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation(entity, entity.getDinosaur().getSystemInfo(), animation), entity.world.provider.getDimension());
        }
    }
}
