package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.ModelComponent;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.projectnublar.client.render.SkeletonBuilderScene;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

@Getter
@Setter
public class SkeletalBuilderBlockEntity extends BaseTaxidermyBlockEntity implements ITickableTileEntity {
    private final ItemStackHandler boneHandler = new ItemStackHandler();
    private final SkeletalProperties skeletalProperties = new SkeletalProperties();
    @OnlyIn(Dist.CLIENT)
    private SkeletonBuilderScene scene;

    private Optional<DinosaurEntity> dinosaurEntity = Optional.empty();

    // Not saved to NBT, player-specific only to help with posing

    private float cameraPitch;
    private float cameraYaw = 90f;
    private double cameraZoom = 1.0;

    public SkeletalBuilderBlockEntity() {
        super(ProjectNublarBlockEntities.SKELETAL_BUILDER.get());
        this.reassureSize();
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        this.dinosaurEntity.ifPresent(d -> nbt.putString("Dinosaur", d.getDinosaur().getRegName().toString()));
        nbt.put("Inventory", this.boneHandler.serializeNBT());
        // save pose data
        nbt.put("SkeletalProperties", this.skeletalProperties.serialize(new CompoundTag()));
        return super.save(nbt);
    }


    @Override
    public void load(BlockState state, CompoundTag nbt) {
        setDinosaur(DinosaurHandler.getRegistry().getValue(new ResourceLocation(nbt.getString("Dinosaur"))));
        this.boneHandler.deserializeNBT(nbt.getCompound("Inventory"));
        // load pose data
        this.reassureSize();
        this.skeletalProperties.deserialize(nbt.getCompound("SkeletalProperties"));
        super.load(state, nbt);
    }

    private void reassureSize() {
        this.getDinosaurEntity().flatMap(d -> d.get(ComponentHandler.SKELETAL_BUILDER)).ifPresent(c -> {
            int size = c.getBoneListed().size();
            if(size != this.boneHandler.getSlots()) {
                this.boneHandler.setSize(size); //TODO: Maybe make a diffrent method that keeps the items if possible?
            }
        });
    }

    public Optional<Dinosaur> getDinosaur() {
        return this.dinosaurEntity.map(DinosaurEntity::getDinosaur);
    }

    @Override
    public ResourceLocation getTexture() {
        return this.dinosaurEntity
                .flatMap(EntityComponentTypes.MODEL.get())
                .map(ModelComponent::getTexture)
                .map(RenderLocationComponent.ConfigurableLocation::getLocation)
                .orElse(TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    public void setDinosaur(Dinosaur dinosaur) {
        if(dinosaur != null) {
            DinosaurEntity entity = dinosaur.createEntity(this.level, dinosaur.getAttacher().emptyConfiguration().withDefaultTypes(false).withType(ComponentHandler.DINOSAUR.get(), ComponentHandler.SKELETAL_BUILDER.get(), EntityComponentTypes.MODEL.get()));
            this.dinosaurEntity = Optional.of(entity);
        }
        this.getHistory().clear();
        this.reassureSize();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public DCMModel getModel() {
        if(!this.dinosaurEntity.isPresent()) {
            return null;
        }
        DinosaurEntity de = this.dinosaurEntity.get();

        return de.getOrExcept(EntityComponentTypes.MODEL).getModelCache();
    }

    public ItemStackHandler getBoneHandler() {
        return boneHandler;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB; //TODO: get size of dinosaur, and fit this to it
    }

    @Override
    public double getViewDistance() {
        return Double.MAX_VALUE;
    }

    public void setCameraAngles(float cameraYaw, float cameraPitch) {
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
    }

    @Override
    public void tick() {
        this.getSkeletalProperties().setPrevRotation(this.getSkeletalProperties().getRotation());
    }
}
