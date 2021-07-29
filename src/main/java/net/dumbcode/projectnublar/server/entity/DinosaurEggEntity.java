package net.dumbcode.projectnublar.server.entity;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.FamilySavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

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
    private DinosaurEggType eggType = DinosaurEggType.EMPTY;
    private UUID familyUUID;
    private int hatchingTicks;

    public DinosaurEggEntity(EntityType<?> eggType, World world) {
        super(eggType, world);
    }

    public DinosaurEggEntity(World world, List<GeneticEntry<?>> combinedGenetics, Dinosaur dinosaur, DinosaurEggType eggType, float randomScaleAdjustment, UUID familyUUID, int hatchingTicks) {
        this(EntityHandler.DINOSAUR_EGG.get(), world);
        this.combinedGenetics.addAll(combinedGenetics);
        this.dinosaur = dinosaur;
        this.eggType = eggType;
        this.randomScaleAdjustment = randomScaleAdjustment;
        this.familyUUID = familyUUID;
        this.hatchingTicks = hatchingTicks;
    }

    @Override
    protected void defineSynchedData() {
        
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
        return EntitySize.fixed(7/16F * this.randomScaleAdjustment, (this.eggType.getEggLength() + 2/16F) * this.randomScaleAdjustment);
    }

    @Override
    public void tick() {
        Vector3d movement = this.getDeltaMovement();
        this.move(MoverType.SELF, movement);

        this.setDeltaMovement(movement.x * 0.8F, movement.y - 0.5F, movement.z * 0.8F);


        if(!this.level.isClientSide && this.tickCount >= this.hatchingTicks) {
            this.kill();
            if(this.dinosaur != null) {
                DinosaurEntity child = this.dinosaur.createEntity(this.level, this.dinosaur.getAttacher().getDefaultConfig()
                    .runBeforeFinalize(EntityComponentTypes.GENETICS.get(), component -> this.combinedGenetics.forEach(component::insertGenetic))
                    .runBeforeFinalize(ComponentHandler.AGE.get(), component -> component.resetStageTo(Dinosaur.CHILD_AGE))

                );

                child.setPos(this.position().x, this.position().y, this.position().z);

                if(this.familyUUID != null) {
                    FamilySavedData.getData(this.familyUUID).getChildren().add(child.getUUID());
                }

                this.level.addFreshEntity(child);

            }
        }
        super.tick();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
//    @Override
//    public boolean canBePushed() {
//        return true;
//    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        this.combinedGenetics.clear();
        StreamUtils.stream(nbt.getList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((CompoundNBT) b))
            .forEach(this.combinedGenetics::add);
        this.dinosaur = DinosaurHandler.getRegistry().getValue(new ResourceLocation(nbt.getString("dinosaur")));
        this.randomScaleAdjustment = nbt.getFloat("random_scale");
        this.eggType = DinosaurEggType.readFromNBT(nbt.getCompound("egg_type"));
        this.familyUUID = nbt.hasUUID("family_uuid") ? nbt.getUUID("family_uuid") : null;
        this.hatchingTicks = nbt.getInt("hatching_ticks");
        this.randomRotation = nbt.getFloat("rot");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.put("genetics", this.combinedGenetics.stream().map(g -> g.serialize(new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        nbt.putString("dinosaur", this.dinosaur.getRegName().toString());
        nbt.putFloat("random_scale", this.randomScaleAdjustment);
        nbt.put("egg_type", DinosaurEggType.writeToNBT(this.eggType));
        if(this.familyUUID != null) {
            nbt.putUUID("family_uuid", this.familyUUID);
        }
        nbt.putInt("hatching_ticks", this.hatchingTicks);
        nbt.putFloat("rot", this.randomRotation);
    }

    @Override
    public void writeSpawnData(PacketBuffer buf) {
        buf.writeFloat(this.randomScaleAdjustment);
        DinosaurEggType.writeToBuf(this.eggType, buf);
    }

    @Override
    public void readSpawnData(PacketBuffer buf) {
        this.randomScaleAdjustment = buf.readFloat();
        this.eggType = DinosaurEggType.readFromBuf(buf);
    }
}
