package net.dumbcode.projectnublar.server.world.structures.structures.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.PlacementSettings;
import net.dumbcode.projectnublar.server.world.structures.structures.placement.StructurePlacement;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;


//Copied and edited from net.minecraft.world.gen.structure.template.Template
public class NBTTemplate
{
    private final List<NBTTemplate.BlockInfo> blocks;
    private final List<NBTTemplate.EntityInfo> entities;

    @Getter private final BlockPos range;

    @Nonnull
    private final StructurePlacement placement;

    public NBTTemplate(List<BlockInfo> blocks, List<EntityInfo> entities, BlockPos range, @Nonnull StructurePlacement placement) {
        this.blocks = blocks;
        this.entities = entities;
        this.range = range;
        this.placement = placement;
    }

    public void addBlocksToWorld(
        World worldIn,
        BlockPos pos,
        StructureInstance instance,
        List<DataHandler> handlers,
        PlacementSettings.Decision settingsDecision,
        Random random,
        BiFunction<BlockPos, BlockInfo, BlockInfo> infoFunc,
        int flags
    ) {
        if (!this.blocks.isEmpty() || !this.entities.isEmpty())  {


            Map<BlockInfo, BlockPos> mappedPositions = Maps.newHashMap();
            for (BlockInfo blockInfo : this.blocks) {
                BlockPos blockpos = transformedBlockPos(blockInfo.pos, settingsDecision);
                mappedPositions.put(blockInfo, placement.transpose(worldIn, instance, blockpos.add(pos), blockpos));
            }

            for (NBTTemplate.BlockInfo template$blockinfo : this.blocks) {
                BlockPos blockpos = mappedPositions.get(template$blockinfo);
                NBTTemplate.BlockInfo template$blockinfo1 = infoFunc.apply(blockpos, template$blockinfo);

                if (template$blockinfo1 != null) {
                    Block block1 = template$blockinfo1.blockState.getBlock();

                    if (block1 != Blocks.STRUCTURE_BLOCK)
                    {
                        IBlockState iblockstate = template$blockinfo1.blockState.withMirror(settingsDecision.getMirror());
                        IBlockState iblockstate1 = iblockstate.withRotation(settingsDecision.getRotation());

                        if (template$blockinfo1.tileentityData != null)
                        {
                            TileEntity tileentity = worldIn.getTileEntity(blockpos);

                            if (tileentity != null)
                            {
                                if (tileentity instanceof IInventory)
                                {
                                    ((IInventory)tileentity).clear();
                                }

                                worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 4);
                            }
                        }

                        if (placement.place(worldIn, instance, blockpos, template$blockinfo.pos, iblockstate1, flags) && template$blockinfo1.tileentityData != null)
                        {
                            TileEntity tileentity2 = worldIn.getTileEntity(blockpos);

                            if (tileentity2 != null)
                            {
                                template$blockinfo1.tileentityData.setInteger("x", blockpos.getX());
                                template$blockinfo1.tileentityData.setInteger("y", blockpos.getY());
                                template$blockinfo1.tileentityData.setInteger("z", blockpos.getZ());
                                tileentity2.readFromNBT(template$blockinfo1.tileentityData);
                                tileentity2.mirror(settingsDecision.getMirror());
                                tileentity2.rotate(settingsDecision.getRotation());
                            }
                        }
                    }
                }
            }

            for (NBTTemplate.BlockInfo info : this.blocks) {
                BlockPos blockpos1 = mappedPositions.get(info);

                IBlockState iblockstate = info.blockState;

                if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK && info.tileentityData != null)
                {
                    TileEntityStructure.Mode tileentitystructure$mode = TileEntityStructure.Mode.valueOf(info.tileentityData.getString("mode"));

                    if (tileentitystructure$mode == TileEntityStructure.Mode.DATA)
                    {
                        for (DataHandler handler : handlers) {
                            IBlockState state = handler.get(info.tileentityData.getString("metadata"), worldIn, blockpos1, random);
                            if(state != null) {
                                worldIn.setBlockState(blockpos1, state);
                                break;
                            }
                        }
                    }
                }

                worldIn.notifyNeighborsRespectDebug(blockpos1, info.blockState.getBlock(), false);

                if (info.tileentityData != null)
                {
                    TileEntity tileentity1 = worldIn.getTileEntity(blockpos1);

                    if (tileentity1 != null)
                    {
                        tileentity1.markDirty();
                    }
                }
            }

            this.addEntitiesToWorld(worldIn, pos, settingsDecision.getMirror(), settingsDecision.getRotation());
        }
    }

    private void addEntitiesToWorld(World worldIn, BlockPos pos, Mirror mirrorIn, Rotation rotationIn)
    {
        for (NBTTemplate.EntityInfo template$entityinfo : this.entities)
        {
            NBTTagCompound nbttagcompound = template$entityinfo.entityData;
            Vec3d vec3d = transformedVec3d(template$entityinfo.pos, mirrorIn, rotationIn);
            Vec3d vec3d1 = vec3d.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.x));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.y));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.z));
            nbttagcompound.setTag("Pos", nbttaglist);
            nbttagcompound.setUniqueId("UUID", UUID.randomUUID());
            Entity entity;

            try
            {
                entity = EntityList.createEntityFromNBT(nbttagcompound, worldIn);
            }
            catch (Exception var15)
            {
                entity = null;
            }

            if (entity != null)
            {
                float f = entity.getMirroredYaw(mirrorIn);
                f = f + (entity.rotationYaw - entity.getRotatedYaw(rotationIn));
                entity.setLocationAndAngles(vec3d1.x, vec3d1.y, vec3d1.z, f, entity.rotationPitch);
                worldIn.spawnEntity(entity);
            }
        }
    }

    public BlockPos transformedBlockPos(BlockPos pos, PlacementSettings.Decision decision) {
        int halfX = (this.range.getX()) / 2;
        int halfZ = (this.range.getZ()) / 2;
        return this.transformedBlockPosAround(pos, halfX, halfZ, decision);
    }

    public BlockPos transformedBlockPosAround(BlockPos pos, int halfX, int halfZ, PlacementSettings.Decision decision)
    {

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

        return out.add(halfX, 0, halfZ);
    }

    private static Vec3d transformedVec3d(Vec3d vec, Mirror mirrorIn, Rotation rotationIn)
    {
        double d0 = vec.x;
        double d1 = vec.y;
        double d2 = vec.z;
        boolean flag = true;

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                d2 = 1.0D - d2;
                break;
            case FRONT_BACK:
                d0 = 1.0D - d0;
                break;
            default:
                flag = false;
        }

        switch (rotationIn)
        {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(d2, d1, 1.0D - d0);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - d2, d1, d0);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - d0, d1, 1.0D - d2);
            default:
                return flag ? new Vec3d(d0, d1, d2) : vec;
        }
    }

    public static NBTTemplate readFromFile(ResourceLocation location, @Nullable StructurePlacement placement) {
        //todo cacheing

        NBTTagCompound compound;

        List<BlockInfo> blocks = Lists.newArrayList();
        List<EntityInfo> entities = Lists.newArrayList();
        try {
            @Cleanup InputStream stream = ProjectNublar.class.getResourceAsStream("/assets/" + location.getNamespace() + "/structures/" + location.getPath() + ".nbt");
            compound = CompressedStreamTools.readCompressed(stream);
        } catch (IOException e) {
            ProjectNublar.getLogger().error("Error loading structure " + location, e);
            return new NBTTemplate(blocks, entities, BlockPos.ORIGIN, StructurePlacement.EMPTY);
        }


        NBTTemplate.BasicPalette template$basicpalette = new NBTTemplate.BasicPalette();
        NBTTagList nbttaglist1 = compound.getTagList("palette", 10);

        for (int i = 0; i < nbttaglist1.tagCount(); ++i)
        {
            template$basicpalette.addMapping(NBTUtil.readBlockState(nbttaglist1.getCompoundTagAt(i)), i);
        }

        NBTTagList nbttaglist3 = compound.getTagList("blocks", 10);


        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        for (int j = 0; j < nbttaglist3.tagCount(); ++j)
        {
            NBTTagCompound nbttagcompound = nbttaglist3.getCompoundTagAt(j);
            NBTTagList nbttaglist2 = nbttagcompound.getTagList("pos", 3);
            BlockPos blockpos = new BlockPos(nbttaglist2.getIntAt(0), nbttaglist2.getIntAt(1), nbttaglist2.getIntAt(2));

            maxX = Math.max(maxX, blockpos.getX());
            maxY = Math.max(maxY, blockpos.getY());
            maxZ = Math.max(maxZ, blockpos.getZ());

            minX = Math.min(minX, blockpos.getX());
            minY = Math.min(minY, blockpos.getY());
            minZ = Math.min(minZ, blockpos.getZ());

            IBlockState iblockstate = template$basicpalette.stateFor(nbttagcompound.getInteger("state"));
            NBTTagCompound nbttagcompound1;

            if (nbttagcompound.hasKey("nbt"))
            {
                nbttagcompound1 = nbttagcompound.getCompoundTag("nbt");
            }
            else
            {
                nbttagcompound1 = null;
            }

            blocks.add(new NBTTemplate.BlockInfo(blockpos, iblockstate, nbttagcompound1));
        }

        NBTTagList nbttaglist4 = compound.getTagList("entities", 10);

        for (int k = 0; k < nbttaglist4.tagCount(); ++k) {
            NBTTagCompound nbttagcompound3 = nbttaglist4.getCompoundTagAt(k);
            NBTTagList nbttaglist5 = nbttagcompound3.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(nbttaglist5.getDoubleAt(0), nbttaglist5.getDoubleAt(1), nbttaglist5.getDoubleAt(2));
            NBTTagList nbttaglist6 = nbttagcompound3.getTagList("blockPos", 3);
            BlockPos blockpos1 = new BlockPos(nbttaglist6.getIntAt(0), nbttaglist6.getIntAt(1), nbttaglist6.getIntAt(2));

            if (nbttagcompound3.hasKey("nbt"))
            {
                NBTTagCompound nbttagcompound2 = nbttagcompound3.getCompoundTag("nbt");
                entities.add(new NBTTemplate.EntityInfo(vec3d, blockpos1, nbttagcompound2));
            }
        }
        return new NBTTemplate(blocks, entities, new BlockPos(maxX - minX, maxY - minY, maxZ - minZ), placement == null ? StructurePlacement.EMPTY : placement);
    }

    static class BasicPalette implements Iterable<IBlockState> {
        public static final IBlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
        final ObjectIntIdentityMap<IBlockState> ids;
        private int lastId;

        private BasicPalette()
        {
            this.ids = new ObjectIntIdentityMap<>(16);
        }

        public int idFor(IBlockState state) {
            int i = this.ids.get(state);

            if (i == -1)
            {
                i = this.lastId++;
                this.ids.put(state, i);
            }

            return i;
        }

        @Nullable
        public IBlockState stateFor(int id) {
            IBlockState iblockstate = this.ids.getByValue(id);
            return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
        }

        public Iterator<IBlockState> iterator()
        {
            return this.ids.iterator();
        }

        public void addMapping(IBlockState p_189956_1_, int p_189956_2_)
        {
            this.ids.put(p_189956_1_, p_189956_2_);
        }
    }

    public static class BlockInfo {
        public final BlockPos pos;
        public final IBlockState blockState;
        public final NBTTagCompound tileentityData;

        public BlockInfo(BlockPos posIn, IBlockState stateIn, @Nullable NBTTagCompound compoundIn) {
            this.pos = posIn;
            this.blockState = stateIn;
            this.tileentityData = compoundIn;
        }
    }

    public static class EntityInfo {
        /** the position the ecs is will be generated to */
        public final Vec3d pos;
        /** None */
        public final BlockPos blockPos;
        /** the serialized NBT data of the ecs in the structure */
        public final NBTTagCompound entityData;

        public EntityInfo(Vec3d vecIn, BlockPos posIn, NBTTagCompound compoundIn) {
            this.pos = vecIn;
            this.blockPos = posIn;
            this.entityData = compoundIn;
        }
    }
}