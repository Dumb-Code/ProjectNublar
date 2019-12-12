package net.dumbcode.projectnublar.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.FamilySavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DinosaurEggEntity extends Entity implements IEntityAdditionalSpawnData {

    private final List<GeneticEntry<?>> combinedGenetics = new ArrayList<>();
    private Dinosaur dinosaur;
    @Getter
    public float randomRotation;
    @Getter
    private float randomScaleAdjustment;
    @Getter
    private DinosaurEggType type = DinosaurEggType.EMPTY;
    private UUID familyUUID;
    private int hatchingTicks;

    public DinosaurEggEntity(World world) {
        super(world);
        this.randomRotation = world.rand.nextFloat() * 360F;
    }

    public DinosaurEggEntity(World world, List<GeneticEntry<?>> combinedGenetics, Dinosaur dinosaur, DinosaurEggType type, float randomScaleAdjustment, UUID familyUUID, int hatchingTicks) {
        this(world);
        this.combinedGenetics.addAll(combinedGenetics);
        this.dinosaur = dinosaur;
        this.type = type;
        this.randomScaleAdjustment = randomScaleAdjustment;
        this.familyUUID = familyUUID;
        this.hatchingTicks = hatchingTicks;
    }


    @Override
    public void onEntityUpdate() {
        this.setSize(7/16F * this.randomScaleAdjustment, (this.type.getEggLength() + 2/16F) * this.randomScaleAdjustment);

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.8F;
        this.motionZ *= 0.8F;
        this.motionY = -0.5F;

        for (Entity entity : this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), input -> true)) {
            entity.applyEntityCollision(this);
        }


        if(!this.world.isRemote && this.ticksExisted >= this.hatchingTicks) {
            this.setDead();
            if(this.dinosaur != null) {
                DinosaurEntity child = this.dinosaur.createEntity(this.world);

                child.setPosition(this.posX, this.posY, this.posZ);

                List<GeneticEntry<?>> entries = child.getOrExcept(EntityComponentTypes.GENETICS).getGenetics();

                entries.replaceAll(geneticEntry -> {
                    for (GeneticEntry<?> genetic : this.combinedGenetics) {
                        if(genetic.getType() == geneticEntry.getType()) {
                            return genetic;
                        }
                    }
                    return geneticEntry;
                });

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
    public boolean canBePushed() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.combinedGenetics.clear();
        StreamUtils.stream(nbt.getTagList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((NBTTagCompound) b))
            .forEach(this.combinedGenetics::add);
        this.dinosaur = ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("dinosaur")));
        this.randomScaleAdjustment = nbt.getFloat("random_scale");
        this.type = DinosaurEggType.readFromNBT(nbt.getCompoundTag("egg_type"));
        this.familyUUID = nbt.hasUniqueId("family_uuid") ? nbt.getUniqueId("family_uuid") : null;
        this.hatchingTicks = nbt.getInteger("hatching_ticks");
        this.randomRotation = nbt.getFloat("rot");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setTag("genetics", this.combinedGenetics.stream().map(g -> g.serialize(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        nbt.setString("dinosaur", this.dinosaur.getRegName().toString());
        nbt.setFloat("random_scale", this.randomScaleAdjustment);
        nbt.setTag("egg_type", DinosaurEggType.writeToNBT(this.type));
        if(this.familyUUID != null) {
            nbt.setUniqueId("family_uuid", this.familyUUID);
        }
        nbt.setInteger("hatching_ticks", this.hatchingTicks);
        nbt.setFloat("rot", this.randomRotation);
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        buf.writeFloat(this.randomScaleAdjustment);
        DinosaurEggType.writeToBuf(this.type, buf);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.randomScaleAdjustment = buf.readFloat();
        this.type = DinosaurEggType.readFromBuf(buf);
    }
}
