package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.client.render.SkeletonBuilderScene;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalHistory;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.Optional;

import static net.dumbcode.projectnublar.server.ProjectNublar.DINOSAUR_REGISTRY;

@Getter
@Setter
public class SkeletalBuilderBlockEntity extends SimpleBlockEntity implements ITickable {
    private final ItemStackHandler boneHandler = new ItemStackHandler();
    private final SkeletalProperties skeletalProperties = new SkeletalProperties();
    @SideOnly(Side.CLIENT)
    private SkeletonBuilderScene scene;

    private Optional<DinosaurEntity> dinosaurEntity = Optional.empty();

    @Getter private final SkeletalHistory history = new SkeletalHistory();


    // Not saved to NBT, player-specific only to help with posing

    private float cameraPitch;
    private float cameraYaw = 90f;
    private double cameraZoom = 1.0;

    public SkeletalBuilderBlockEntity() {
        this.reassureSize();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.dinosaurEntity.ifPresent(d -> nbt.setString("Dinosaur", d.getDinosaur().getRegName().toString()));
        nbt.setTag("Inventory", this.boneHandler.serializeNBT());
        nbt.setTag("History", history.writeToNBT(new NBTTagCompound()));

        // save pose data
        nbt.setTag("SkeletalProperties", this.skeletalProperties.serialize(new NBTTagCompound()));
        return super.writeToNBT(nbt);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        setDinosaur(DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("Dinosaur"))));
        this.boneHandler.deserializeNBT(nbt.getCompoundTag("Inventory"));
        // load pose data
        this.reassureSize();
        this.history.readFromNBT(nbt.getCompoundTag("History"));
        this.skeletalProperties.deserialize(nbt.getCompoundTag("SkeletalProperties"));
        super.readFromNBT(nbt);
    }

    private void reassureSize() {
        this.getDinosaur().ifPresent(dinosaur -> {
            int size = dinosaur.getSkeletalInformation().getBoneListed().size();
            if(size != this.boneHandler.getSlots()) {
                this.boneHandler.setSize(size); //TODO: Maybe make a diffrent method that keeps the items?
            }
        });
    }


    public Optional<Dinosaur> getDinosaur() {
        return this.dinosaurEntity.map(DinosaurEntity::getDinosaur);
    }

    public ResourceLocation getTexture() {
        Optional<Dinosaur> dinosaur = this.getDinosaur();
        return (dinosaur.isPresent() && this.dinosaurEntity.isPresent()) ? dinosaur.get().getTextureLocation(this.dinosaurEntity.get()) : TextureMap.LOCATION_MISSING_TEXTURE;
    }

    public void setDinosaur(Dinosaur dinosaur) {
        if(dinosaur != null) {
            DinosaurEntity entity = dinosaur.createEntity(this.world);
            entity.attachComponent(EntityComponentTypes.SKELETAL_BUILDER);
            entity.getOrExcept(EntityComponentTypes.SKELETAL_BUILDER).stage = ModelStage.SKELETON; //TODO: change life?
            entity.get(EntityComponentTypes.AGE).ifPresent(age -> age.stage = ModelStage.SKELETON);
            this.dinosaurEntity = Optional.of(entity);
        }
        this.history.clear();
        this.reassureSize();
    }

    public TabulaModel getModel() {
        if(!this.dinosaurEntity.isPresent()) {
            return null;
        }
        DinosaurEntity de = this.dinosaurEntity.get();
        return de.getDinosaur().getModelContainer().getModelMap().get(de.getOrExcept(EntityComponentTypes.SKELETAL_BUILDER).stage);
    }

    public ItemStackHandler getBoneHandler() {
        return boneHandler;
    }


    public Map<String, Vector3f> getPoseData() {
        //todo caching
        Map<String, Vector3f> map = Maps.newHashMap();

        this.history.getHistory().forEach(recordList -> recordList.forEach(record -> {
            if(record.getPart().equals(SkeletalHistory.RESET_NAME)) {
                map.clear();
            } else {
                map.put(record.getPart(), new Vector3f(record.getAngle()));
            }
        }));

        for (Map.Entry<String, SkeletalHistory.Edit> entry : this.history.getEditingData().entrySet()) {
            Vector3f vec = map.computeIfAbsent(entry.getKey(), s -> new Vector3f());
            SkeletalHistory.Edit edit = entry.getValue();
            switch (edit.getAxis()) {
                case X_AXIS:
                    vec.x = edit.getAngle();
                    break;
                case Y_AXIS:
                    vec.y = edit.getAngle();
                    break;
                case Z_AXIS:
                    vec.z = edit.getAngle();
                    break;
            }
        }

        return map;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB; //TODO: get size of dinosaur, and fit this to it
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    public void setCameraAngles(float cameraYaw, float cameraPitch) {
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
    }

    @Override
    public void update() {
        this.getSkeletalProperties().setPrevRotation(this.getSkeletalProperties().getRotation());
    }
}
