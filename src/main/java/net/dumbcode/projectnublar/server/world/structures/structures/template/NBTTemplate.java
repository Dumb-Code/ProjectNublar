package net.dumbcode.projectnublar.server.world.structures.structures.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;

import com.google.common.collect.Maps;
import lombok.Cleanup;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.PnPlacementSettings;
import net.dumbcode.projectnublar.server.world.structures.structures.placement.StructurePlacement;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.IClearable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBTTemplate {

    private final Template delegate;

    @Nonnull
    private final StructurePlacement placement;

    private NBTTemplate(Template delegate, StructurePlacement placement) {
        this.delegate = delegate;
        this.placement = placement;
    }

    public void addBlocksToWorld(
        ServerWorld worldIn,
        BlockPos pos,
        StructureInstance instance,
        List<DataHandler> handlers,
        StructureConstants.Decision decision,
        PnPlacementSettings.Decision settingsDecision,
        Random random,
        BiFunction<BlockPos, Template.BlockInfo, Template.BlockInfo> infoFunc,
        int flags //20
    ) {
        List<Template.BlockInfo> blocks = this.delegate.palettes.get(random.nextInt(this.delegate.palettes.size())).blocks();
        Map<Template.BlockInfo, BlockPos> mappedPositions = Maps.newHashMap();
        Map<Template.EntityInfo, Vector3d> mappedEntityPositions = Maps.newHashMap();
        for (Template.BlockInfo blockInfo : blocks) {
            BlockPos blockpos = transformedBlockPos(blockInfo.pos, settingsDecision);
            mappedPositions.put(blockInfo, this.placement.transpose(worldIn, instance, blockpos.offset(pos), blockpos));
        }

        for (Template.EntityInfo entityInfo : this.delegate.entityInfoList) {
            Vector3d vector3d = transformedVec3d(entityInfo.pos, settingsDecision.getMirror(), settingsDecision.getRotation());

            BlockPos b = new BlockPos(vector3d);
            double diffX = vector3d.x - b.getX();
            double diffY = vector3d.y - b.getY();
            double diffZ = vector3d.z - b.getZ();

            BlockPos transpose = this.placement.transpose(worldIn, instance, b.offset(pos), b);

            mappedEntityPositions.put(entityInfo, new Vector3d(transpose.getX() + diffX, transpose.getY() + diffY, transpose.getZ() + diffZ));
        }

        for (Template.BlockInfo blockInfo : blocks) {
            BlockPos blockPos = mappedPositions.get(blockInfo);
            Template.BlockInfo apply = infoFunc.apply(blockPos, blockInfo);

            if(apply != null) {
                if(apply.state.getBlock() == Blocks.STRUCTURE_BLOCK) {
                    continue;
                }
                BlockState state = apply.state
                    .rotate(worldIn, blockPos, settingsDecision.getRotation())
                    .mirror(settingsDecision.getMirror());

                if (blockInfo.nbt != null) {
                    TileEntity tileentity = worldIn.getBlockEntity(blockPos);
                    IClearable.tryClear(tileentity);
                    worldIn.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), flags);
                }

                if (this.placement.place(worldIn, instance, blockPos, blockInfo.pos, state, flags)) {
                    if (blockInfo.nbt != null) {
                        TileEntity tileentity1 = worldIn.getBlockEntity(blockPos);
                        if (tileentity1 != null) {
                            blockInfo.nbt.putInt("x", blockPos.getX());
                            blockInfo.nbt.putInt("y", blockPos.getY());
                            blockInfo.nbt.putInt("z", blockPos.getZ());
                            tileentity1.load(state, blockInfo.nbt);
                            tileentity1.mirror(settingsDecision.getMirror());
                            tileentity1.rotate(settingsDecision.getRotation());
                        }
                    }
                }

            }
        }

        for (Template.BlockInfo info : blocks) {
            BlockPos blockPos = mappedPositions.get(info);
            if(info.state.getBlock() == Blocks.STRUCTURE_BLOCK && info.nbt != null) {
                if(StructureMode.valueOf(info.nbt.getString("mode")) == StructureMode.DATA) {
                    for (DataHandler handler : handlers) {
                        BlockState state = handler.get(info.nbt.getString("metadata"), worldIn, blockPos, random, decision);
                        if(state != null) {
                            worldIn.setBlock(blockPos, state, flags);
                            break;
                        }
                    }
                }
            }
        }

        for(Template.EntityInfo entityInfo : this.delegate.entityInfoList) {
            Vector3d vec3 = mappedEntityPositions.get(entityInfo);
            CompoundNBT compoundnbt = entityInfo.nbt.copy();
            ListNBT listnbt = new ListNBT();
            listnbt.add(DoubleNBT.valueOf(vec3.x));
            listnbt.add(DoubleNBT.valueOf(vec3.y));
            listnbt.add(DoubleNBT.valueOf(vec3.z));
            compoundnbt.put("Pos", listnbt);
            compoundnbt.remove("UUID");
            try {
                EntityType.create(compoundnbt, worldIn).ifPresent(entity -> {
                    float f = entity.mirror(settingsDecision.getMirror());
                    f = f + (entity.yRot - entity.rotate(settingsDecision.getRotation()));
                    entity.moveTo(vec3.x, vec3.y, vec3.z, f, entity.xRot);
                    if (entity instanceof MobEntity) {
                        ((MobEntity)entity).finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(new BlockPos(vec3)), SpawnReason.STRUCTURE, (ILivingEntityData)null, compoundnbt);
                    }

                    worldIn.addFreshEntityWithPassengers(entity);
                });
            } catch (Exception ignored) {
            }
        }
    }

    private BlockPos transformedBlockPos(BlockPos pos, PnPlacementSettings.Decision decision) {
        BlockPos size = this.delegate.getSize(decision.getRotation());
        int halfX = (size.getX()) / 2;
        int halfZ = (size.getZ()) / 2;
        return this.transformedBlockPosAround(pos, halfX, halfZ, decision);
    }

    private static Vector3d transformedVec3d(Vector3d vec, Mirror mirrorIn, Rotation rotationIn) {
        double d0 = vec.x;
        double d1 = vec.y;
        double d2 = vec.z;
        boolean flag = true;

        switch (mirrorIn) {
            case LEFT_RIGHT:
                d2 = 1.0D - d2;
                break;
            case FRONT_BACK:
                d0 = 1.0D - d0;
                break;
            default:
                flag = false;
        }

        switch (rotationIn) {
            case COUNTERCLOCKWISE_90:
                return new Vector3d(d2, d1, 1.0D - d0);
            case CLOCKWISE_90:
                return new Vector3d(1.0D - d2, d1, d0);
            case CLOCKWISE_180:
                return new Vector3d(1.0D - d0, d1, 1.0D - d2);
            default:
                return flag ? new Vector3d(d0, d1, d2) : vec;
        }
    }

    public BlockPos transformedBlockPosAround(BlockPos pos, int halfX, int halfZ, PnPlacementSettings.Decision decision) {
        int i = pos.getX() - halfX;
        int j = pos.getY();
        int k = pos.getZ() - halfZ;
        boolean flag = true;

        switch (decision.getMirror())
        {
            case LEFT_RIGHT:
                k = -k;
                break;
            case FRONT_BACK:
                i = -i;
                break;
            default:
                flag = false;
        }

        BlockPos out;

        switch (decision.getRotation())
        {
            case COUNTERCLOCKWISE_90:
                out = new BlockPos(k, j, -i);
                break;
            case CLOCKWISE_90:
                out = new BlockPos(-k, j, i);
                break;
            case CLOCKWISE_180:
                out = new BlockPos(-i, j, -k);
                break;
            default:
                if(!flag) {
                    return pos;
                }
                out = new BlockPos(i, j, k);
        }

        return out.offset(halfX, 0, halfZ) ;
    }

    public static NBTTemplate readFromFile(ResourceLocation location, @Nullable StructurePlacement placement) {
        Template template = new Template();
        try {
            @Cleanup InputStream stream = ProjectNublar.class.getResourceAsStream("/assets/" + location.getNamespace() + "/structures/" + location.getPath() + ".nbt");
            template.load(CompressedStreamTools.readCompressed(stream));
        } catch (IOException e) {
            ProjectNublar.LOGGER.error("Error loading structure " + location, e);
        }

        return new NBTTemplate(template, placement == null ? StructurePlacement.EMPTY : placement);
    }

    public BlockPos getRange() {
        return this.delegate.getSize();
    }
}
