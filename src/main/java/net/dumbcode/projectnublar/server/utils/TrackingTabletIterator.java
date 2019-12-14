package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.S22OpenTrackingTabletGui;
import net.dumbcode.projectnublar.server.network.S24TrackingTabletUpdateChunk;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class TrackingTabletIterator {
    public static final Map<UUID, TrackingTabletIterator> PLAYER_TO_TABLET_MAP = Maps.newHashMap();

    private final EntityPlayerMP player;
    private final BlockPos fromPos;
    private final BlockPos toPos;

    private final World world;

    private ChunkPos currentPos;

    public TrackingTabletIterator(EntityPlayerMP player, BlockPos center, int squareRadius) {
        this.player = player;
        this.world = player.world;

        squareRadius = Math.abs(squareRadius);
        this.fromPos = center.add(-squareRadius, 0, -squareRadius);
        this.toPos = center.add(squareRadius, 0, squareRadius);

        PLAYER_TO_TABLET_MAP.compute(player.getUniqueID(), (uuid, iterator) -> {
            if(iterator != null) {
                iterator.finish();
            }
            return this;
        });

        this.currentPos = new ChunkPos(this.fromPos);

        //Start the handshaking
        ProjectNublar.NETWORK.sendTo(new S22OpenTrackingTabletGui(this.fromPos.getX(), this.toPos.getX(), this.fromPos.getZ(), this.toPos.getZ()), this.player);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            this.doCurrentChunk();
            this.incrementPosition();

            //After 20ms, do no more
            if(System.currentTimeMillis() - startTime >= 20) {
                break;
            }
        }

    }

    private void doCurrentChunk() {
        //All values are inclusive.
        int xStart = MathHelper.clamp(this.currentPos.getXStart(), this.fromPos.getX(), this.toPos.getX());
        int xEnd = MathHelper.clamp(this.currentPos.getXEnd(), this.fromPos.getX(), this.toPos.getX());

        int zStart = MathHelper.clamp(this.currentPos.getZStart(), this.fromPos.getZ(), this.toPos.getZ());
        int zEnd = MathHelper.clamp(this.currentPos.getZEnd(), this.fromPos.getZ(), this.toPos.getZ());

        int[] colorData = new int[(xEnd - xStart + 1)*(zEnd - zStart + 1)];
        Biome[] biomes = this.world.getBiomeProvider().getBiomes(null, xStart-1, zStart-1, xEnd - xStart + 3, zEnd - zStart + 3, false);

        this.generateBiomeData(xStart, zStart, xEnd, zEnd, colorData, biomes);

        if(this.world.isChunkGeneratedAt(this.currentPos.x, this.currentPos.z)) {
            this.generateBlockMapData(xStart, zStart, xEnd, zEnd, colorData, biomes);
        }

        ProjectNublar.NETWORK.sendTo(new S24TrackingTabletUpdateChunk(xStart, xEnd, zStart, zEnd, colorData), this.player);
    }

    private void generateBlockMapData(int xStart, int zStart, int xEnd, int zEnd, int[] colorData, Biome[] biomes) {
        int[] biomeData = Arrays.copyOf(colorData, colorData.length);
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();

        for (int z = zStart; z <= zEnd ; z++) {
            for (int x = xStart; x <= xEnd ; x++) {
                BlockPos.MutableBlockPos blockPos = this.getTopBlock(pos.setPos(x, 0, z));
                IBlockState state = this.world.getBlockState(blockPos);

                int colorIndex = 1;
                if(state.isFullBlock() && this.world.isBlockLoaded(blockPos.north())) {
                    int prevHeight = blockPos.getY() - this.getTopBlock(blockPos.move(EnumFacing.NORTH)).getY();
                    colorIndex = MathHelper.clamp(prevHeight, -1, 1) + 1;
                }

                int color;
                //todo: config these blocks
                if(state.getBlock() == Blocks.GRASS) {
                    color = BiomeUtils.getGrassColor(biomes[this.getIndex(xStart-1, xEnd+1, zStart-1, x, z)], blockPos, 0.5F);
                } else if (state.getBlock() == Blocks.LEAVES || state.getBlock() == Blocks.LEAVES2) {
                    color = BiomeUtils.getGrassColor(biomes[this.getIndex(xStart-1, xEnd+1, zStart-1, x, z)], blockPos, 0.3F);
                } else {
                    color = state.getMapColor(this.world, blockPos).colorValue;
                }
                colorData[this.getIndex(xStart, xEnd, zStart, x, z)] = this.getMapColor(color, colorIndex);
            }
        }
        pos.release();

        boolean topCorner = this.isCornerGenerated(0, -1);
        boolean leftCorner = this.isCornerGenerated(-1, 0);
        boolean rightCorner = this.isCornerGenerated(1, 0);
        boolean downCorner = this.isCornerGenerated(0, 1);

        boolean[] generatedGrid = {
            leftCorner && this.isCornerGenerated(-1, -1) && topCorner, topCorner,  topCorner && this.isCornerGenerated(1, -1) && rightCorner,
            leftCorner,                   /*Self will always be generated*/ true, rightCorner,
            leftCorner && this.isCornerGenerated(-1, 1) && downCorner, downCorner, downCorner && this.isCornerGenerated(1, 1) && rightCorner,
        };

        boolean needsGradient = false;
        for (boolean point : generatedGrid) {
            if(!point) {
                needsGradient = true;
                break;
            }
        }

        if (needsGradient) {
            this.generateGradient(xStart, xEnd, zStart, zEnd, generatedGrid, colorData, biomeData);
        }
    }

    private void generateGradient(int xStart, int xEnd, int zStart, int zEnd, boolean[] generatedGrid, int[] colorData, int[] biomeData) {
        for (int quadX = 0; quadX < 2; quadX++) {
            for (int quadZ = 0; quadZ < 2; quadZ++) {

                int worldStartX = this.currentPos.getXStart() + quadX*8;
                int worldStartZ = this.currentPos.getZStart() + quadZ*8;

                int worldEndX = worldStartX + 7;
                int worldEndZ = worldStartZ + 7;

                //Check the quad isn't outside of the world view
                if(worldEndX < this.fromPos.getX() || worldEndZ < this.fromPos.getZ() || worldStartX > this.toPos.getX() || worldStartZ > this.toPos.getZ()) {
                    continue;
                }

                int topLeft = quadX + quadZ*3;
                int topRight = topLeft + 1;
                int bottomLeft = topLeft + 3;
                int bottomRight = bottomLeft + 1;

                int corner00 = generatedGrid[topLeft] ? 1 : 0;
                int corner01 = generatedGrid[topRight] ? 1 : 0;
                int corner10 = generatedGrid[bottomLeft] ? 1 : 0;
                int corner11 = generatedGrid[bottomRight] ? 1 : 0;

                int sum = corner00 + corner01 + corner10 + corner11;

                //There'll never be a senario of:
                // 1 -- 0
                // |    |
                // 0 -- 1
                //So we can just do this to check if we should just straight line interpolate
                if(sum % 2 == 0) {
                    this.generateStraightEdges(
                        xStart, xEnd, zStart, zEnd,
                        worldStartZ, worldEndZ, worldStartX, worldEndX,
                        corner00 | corner01, corner10 | corner11, corner00 | corner10, corner01 | corner11,
                        colorData, biomeData
                    );
                } else {
                    this.generateCurvedEdge(
                        xStart, xEnd, zStart, zEnd,
                        worldStartZ, worldEndZ, worldStartX, worldEndX,
                        this.findVertex(sum == 1 ? 1 : 0, corner00, corner01, corner10, corner11), sum == 1 ? 1 : 0, sum == 1 ? -1 : 1,
                        colorData, biomeData
                    );
                }

            }
        }
    }

    private int findVertex(int search, int... vertices) {
        for (int i = 0; i < vertices.length; i++) {
            if(vertices[i] == search) {
                return i;
            }
        }
        throw new IllegalStateException("Could not find value '" + search + "' in array: " + Arrays.toString(vertices));
    }

    private void generateCurvedEdge(
        int xStart, int xEnd, int zStart, int zEnd,
        int worldStartZ, int worldEndZ, int worldStartX, int worldEndX,
        int vertex, int offset, int modifier, //vertex => 00, 01, 10, 11
        int[] colorData, int[] biomeData
    ) {
        for (int z = Math.max(zStart, worldStartZ); z <= Math.min(zEnd, worldEndZ) ; z++) {
            for (int x = Math.max(xStart, worldStartX); x <= Math.min(xEnd, worldEndX); x++) {
                float xDist = (x - worldStartX) / 8F - vertex % 2;
                float zDist = (z - worldStartZ) / 8F - Math.floorDiv(vertex, 2);

                float alpha = (float) (offset + modifier*MathHelper.clamp(Math.sqrt(xDist*xDist + zDist*zDist), 0D, 1D));

                int index = this.getIndex(xStart, xEnd, zStart, x, z);
                colorData[index] = this.interpolateBlockBiome(colorData[index], biomeData[index], alpha);
            }
        }
    }

    private void generateStraightEdges(
        int xStart, int xEnd, int zStart, int zEnd,
        int worldStartZ, int worldEndZ, int worldStartX, int worldEndX,
        int top, int bottom, int left, int right,
        int[] colorData, int[] biomeData
    ) {
        for (int z = Math.max(zStart, worldStartZ); z <= Math.min(zEnd, worldEndZ) ; z++) {
            for (int x = Math.max(xStart, worldStartX); x <= Math.min(xEnd, worldEndX); x++) {
                float xAlphaPosition = (x - worldStartX) / 8F;
                float zAlphaPosition = (z - worldStartZ) / 8F;

                float xAlpha = left + (right - left) * xAlphaPosition;
                float zAlpha = top + (bottom - top) * zAlphaPosition;

                int index = this.getIndex(xStart, xEnd, zStart, x, z);
                colorData[index] = this.interpolateBlockBiome(colorData[index], biomeData[index], xAlpha * zAlpha);
            }
        }
    }

    private int interpolateBlockBiome(int block, int biome, float alpha) {
        int blockR = (block >> 16) & 255;
        int blockG = (block >> 8) & 255;
        int blockB = block & 255;

        int biomeR = (biome >> 16) & 255;
        int biomeG = (biome >> 8) & 255;
        int biomeB = biome & 255;

        int red = (int) (biomeR + (blockR - biomeR) * alpha);
        int green = (int) (biomeG + (blockG - biomeG) * alpha);
        int blue = (int) (biomeB + (blockB - biomeB) * alpha);

        return (red << 16) | (green << 8) | (blue);

    }

    private boolean isCornerGenerated(int xOff, int zOff) {
        return this.world.isChunkGeneratedAt(this.currentPos.x + xOff, this.currentPos.z + zOff);
    }

    private void generateBiomeData(int xStart, int zStart, int xEnd, int zEnd, int[] colorData, Biome[] biomes) {
        for (int z = zStart; z <= zEnd ; z++) {
            for (int x = xStart; x <= xEnd; x++) {

                Biome biome = biomes[this.getIndex(xStart-1, xEnd + 1, zStart-1, x, z)];

                boolean bordered = false;

                for (int btx = x-1; btx < x+2; btx++) {
                    for (int btz = z-1; btz < z+2; btz++) {
                        if(btx >= this.fromPos.getX() && btx <= this.toPos.getX() && btz >= this.fromPos.getZ() && btz <= this.toPos.getZ()) {
                            bordered |= biomes[this.getIndex(xStart-1, xEnd+1, zStart-1, btx, btz)] != biome;
                        }

                    }
                }


                if(!bordered) {
                }
                colorData[this.getIndex(xStart, xEnd, zStart, x, z)] = BiomeUtils.getBiomeColor(new BlockPos(x, 100, z), biome);
            }
        }
    }

    private int getIndex(int xStart, int xEnd, int zStart, int x, int z) {
        return (x - xStart) + (z - zStart)*(xEnd - xStart + 1);
    }

    private int getMapColor(int color, int index) {
        int alpha = 220;

        if (index == 3) {
            alpha = 135;
        }

        if (index == 2) {
            alpha = 255;
        }

        if (index == 0) {
            alpha = 180;
        }

        int r = (color >> 16 & 255) * alpha / 255;
        int g = (color >> 8 & 255) * alpha / 255;
        int b = (color & 255) * alpha / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private BlockPos.MutableBlockPos getTopBlock(BlockPos.MutableBlockPos topPos) {
        topPos.setY(256);
        while(topPos.getY() >= 1) {
            IBlockState state = this.world.getBlockState(topPos);
            if(state.isFullBlock() || state.getMaterial().isLiquid()) {
                break;
            }
            topPos.setY(topPos.getY() - 1);
        }
        return topPos;
    }

    //Returns false when done
    private boolean incrementPosition() {
        this.currentPos = new ChunkPos(this.currentPos.x + 1, this.currentPos.z);

        //Out of bounds to the right, so move down one
        if(this.currentPos.getXStart() > this.toPos.getX()) {
            this.currentPos = new ChunkPos(this.fromPos.getX() >> 4, this.currentPos.z + 1);
        }

        //Out of bounds to the bottom, so we're done and should return false
        return this.currentPos.getZStart() <= this.toPos.getZ();
    }

    public void start() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void finish() {
        MinecraftForge.EVENT_BUS.unregister(this);
        PLAYER_TO_TABLET_MAP.remove(this.player.getUniqueID());
    }

}
