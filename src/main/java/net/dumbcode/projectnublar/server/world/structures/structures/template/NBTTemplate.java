package net.dumbcode.projectnublar.server.world.structures.structures.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.world.structures.structures.template.placement.TemplatePlacement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;


//Copied and edited from net.minecraft.world.gen.structure.template.Template
public class NBTTemplate
{
    private final List<NBTTemplate.BlockInfo> blocks;
    private final List<NBTTemplate.EntityInfo> entities;

    @Getter private final BlockPos minimum;
    @Getter private final BlockPos maximum;

    @Nullable private final TemplatePlacement placement;

    public NBTTemplate(List<BlockInfo> blocks, List<EntityInfo> entities, BlockPos minimum, BlockPos maximum, @Nullable TemplatePlacement placement) {
        this.blocks = blocks;
        this.entities = entities;
        this.minimum = minimum;
        this.maximum = maximum;
        this.placement = placement;
    }

    public BlockPos transformedBlockPos(PlacementSettings placementIn, BlockPos pos) {
        return transformedBlockPos(pos, placementIn.getMirror(), placementIn.getRotation());
    }

    public void addBlocksToWorld(World worldIn, BlockPos pos, BiFunction<BlockPos, BlockInfo, BlockInfo> infoFunc, PlacementSettings placementIn, int flags) {
        if (!this.blocks.isEmpty() || !placementIn.getIgnoreEntities() && !this.entities.isEmpty())  {
            Block block = placementIn.getReplacedBlock();
            StructureBoundingBox structureboundingbox = placementIn.getBoundingBox();

            Map<BlockInfo, BlockPos> mappedPositions = Maps.newHashMap();
            for (BlockInfo blockInfo : this.blocks) {
                BlockPos blockpos = transformedBlockPos(placementIn, blockInfo.pos);
                mappedPositions.put(blockInfo, this.placement == null ? blockpos.add(pos) : this.placement.transpose(worldIn, blockpos.add(pos), blockpos));
            }

            for (NBTTemplate.BlockInfo template$blockinfo : this.blocks) {
                BlockPos blockpos = mappedPositions.get(template$blockinfo);
                NBTTemplate.BlockInfo template$blockinfo1 = infoFunc.apply(blockpos, template$blockinfo);

                if (template$blockinfo1 != null) {
                    Block block1 = template$blockinfo1.blockState.getBlock();

                    if ((block == null || block != block1) && (!placementIn.getIgnoreStructureBlock() || block1 != Blocks.STRUCTURE_BLOCK) && (structureboundingbox == null || structureboundingbox.isVecInside(blockpos)))
                    {
                        IBlockState iblockstate = template$blockinfo1.blockState.withMirror(placementIn.getMirror());
                        IBlockState iblockstate1 = iblockstate.withRotation(placementIn.getRotation());

                        if (template$blockinfo1.tileentityData != null)
                        {
                            TileEntity tileentity = worldIn.getTileEntity(blockpos);

                            if (tileentity != null)
                            {
                                if (tileentity instanceof IInventory)
                                {
                                    ((IInventory)tileentity).clear();
                                }

                                worldIn.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 4);
                            }
                        }

                        if (worldIn.setBlockState(blockpos, iblockstate1, flags) && template$blockinfo1.tileentityData != null)
                        {
                            TileEntity tileentity2 = worldIn.getTileEntity(blockpos);

                            if (tileentity2 != null)
                            {
                                template$blockinfo1.tileentityData.setInteger("x", blockpos.getX());
                                template$blockinfo1.tileentityData.setInteger("y", blockpos.getY());
                                template$blockinfo1.tileentityData.setInteger("z", blockpos.getZ());
                                tileentity2.readFromNBT(template$blockinfo1.tileentityData);
                                tileentity2.mirror(placementIn.getMirror());
                                tileentity2.rotate(placementIn.getRotation());
                            }
                        }
                    }
                }
            }

            for (NBTTemplate.BlockInfo template$blockinfo2 : this.blocks)
            {
                if (block == null || block != template$blockinfo2.blockState.getBlock())
                {
                    BlockPos blockpos1 = mappedPositions.get(template$blockinfo2);

                    if (structureboundingbox == null || structureboundingbox.isVecInside(blockpos1))
                    {
                        worldIn.notifyNeighborsRespectDebug(blockpos1, template$blockinfo2.blockState.getBlock(), false);

                        if (template$blockinfo2.tileentityData != null)
                        {
                            TileEntity tileentity1 = worldIn.getTileEntity(blockpos1);

                            if (tileentity1 != null)
                            {
                                tileentity1.markDirty();
                            }
                        }
                    }
                }
            }

            if (!placementIn.getIgnoreEntities())
            {
                this.addEntitiesToWorld(worldIn, pos, placementIn.getMirror(), placementIn.getRotation(), structureboundingbox);
            }
        }
    }

    private void addEntitiesToWorld(World worldIn, BlockPos pos, Mirror mirrorIn, Rotation rotationIn, @Nullable StructureBoundingBox aabb)
    {
        for (NBTTemplate.EntityInfo template$entityinfo : this.entities)
        {
            BlockPos blockpos = transformedBlockPos(template$entityinfo.blockPos, mirrorIn, rotationIn).add(pos);

            if (aabb == null || aabb.isVecInside(blockpos))
            {
                NBTTagCompound nbttagcompound = template$entityinfo.entityData;
                Vec3d vec3d = transformedVec3d(template$entityinfo.pos, mirrorIn, rotationIn);
                Vec3d vec3d1 = vec3d.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
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
    }

    private BlockPos transformedBlockPos(BlockPos pos, Mirror mirrorIn, Rotation rotationIn)
    {
        int i = pos.getX() - (this.maximum.getX() - this.minimum.getX()) / 2;
        int j = pos.getY();
        int k = pos.getZ() - (this.maximum.getZ() - this.minimum.getZ()) / 2;
        boolean flag = true;

        switch (mirrorIn)
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

        switch (rotationIn)
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
                out = flag ? new BlockPos(i, j, k) : pos;
        }

        return out.add((this.maximum.getX() - this.minimum.getX()) / 2, 0, (this.maximum.getZ() - this.minimum.getZ()) / 2);
    }

    public int getSizeX() {
        return this.maximum.getX() - this.minimum.getX();
    }

    public int getSizeY() {
        return this.maximum.getY() - this.minimum.getY();
    }

    public int getSizeZ() {
        return this.maximum.getZ() - this.minimum.getZ();
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

    public static NBTTemplate readFromFile(ResourceLocation location, TemplatePlacement placement) {
        //todo cacheing

        NBTTagCompound compound;

        List<BlockInfo> blocks = Lists.newArrayList();
        List<EntityInfo> entities = Lists.newArrayList();
        try {
            @Cleanup InputStream stream = ProjectNublar.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/structures/" + location.getResourcePath() + ".nbt");
            compound = CompressedStreamTools.readCompressed(stream);
        } catch (IOException e) {
            ProjectNublar.getLogger().error("Error loading structure " + location, e);
            return new NBTTemplate(blocks, entities, BlockPos.ORIGIN, BlockPos.ORIGIN, null);
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
        return new NBTTemplate(blocks, entities, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), placement);
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
        /** the position the entity is will be generated to */
        public final Vec3d pos;
        /** None */
        public final BlockPos blockPos;
        /** the serialized NBT data of the entity in the structure */
        public final NBTTagCompound entityData;

        public EntityInfo(Vec3d vecIn, BlockPos posIn, NBTTagCompound compoundIn) {
            this.pos = vecIn;
            this.blockPos = posIn;
            this.entityData = compoundIn;
        }
    }
}