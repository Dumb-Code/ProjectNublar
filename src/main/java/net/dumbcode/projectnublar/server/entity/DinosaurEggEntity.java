package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.FamilySavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DinosaurEggEntity extends Entity {

    private final List<GeneticEntry<?>> combinedGenetics = new ArrayList<>();
    private Dinosaur dinosaur;
    private UUID familyUUID;
    private int hatchingTicks;

    public DinosaurEggEntity(World world) {
        super(world);
    }

    public DinosaurEggEntity(World world, List<GeneticEntry<?>> combinedGenetics, Dinosaur dinosaur, UUID familyUUID, int hatchingTicks) {
        this(world);
        this.combinedGenetics.addAll(combinedGenetics);
        this.dinosaur = dinosaur;
        this.familyUUID = familyUUID;
        this.hatchingTicks = hatchingTicks;
    }


    @Override
    public void onEntityUpdate() {
        if(!this.world.isRemote && this.ticksExisted >= this.hatchingTicks) {
            this.setDead();
            if(this.dinosaur != null) {
                DinosaurEntity child = this.dinosaur.createEntity(this.world);

                child.setPosition(this.posX, this.posY, this.posZ);

                List<GeneticEntry<?>> entries = child.getOrExcept(EntityComponentTypes.GENETICS).getGenetics();
                entries.clear();

                entries.addAll(this.combinedGenetics);

                if(this.familyUUID != null) {
                    FamilySavedData.getData(this.familyUUID).getChildren().add(child.getUniqueID());
                }
                child.get(ComponentHandler.AGE).ifPresent(a -> a.resetStageTo(Dinosaur.CHILD_AGE));

                child.finalizeComponents();

                this.world.spawnEntity(child);

            }
        }
        super.onEntityUpdate();
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.combinedGenetics.clear();
        StreamUtils.stream(nbt.getTagList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((NBTTagCompound) b))
            .forEach(this.combinedGenetics::add);
        this.dinosaur = ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("dinosaur")));
        this.familyUUID = nbt.hasUniqueId("family_uuid") ? nbt.getUniqueId("family_uuid") : null;
        this.hatchingTicks = nbt.getInteger("hatching_ticks");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setTag("genetics", this.combinedGenetics.stream().map(g -> g.serialize(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        nbt.setString("dinosaur", this.dinosaur.getRegName().toString());
        if(this.familyUUID != null) {
            nbt.setUniqueId("family_uuid", this.familyUUID);
        }
        nbt.setInteger("hatching_ticks", this.hatchingTicks);
    }
}
