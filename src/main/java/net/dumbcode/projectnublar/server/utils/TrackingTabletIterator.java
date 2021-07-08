package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.dumbcode.projectnublar.server.network.S2STrackingTabletUpdateChunk;
import net.dumbcode.projectnublar.server.network.S2CStartTrackingTabletHandshake;
import net.dumbcode.projectnublar.server.network.S2CSetTrackingDataList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrackingTabletIterator {
    public static final Map<UUID, TrackingTabletIterator> PLAYER_TO_TABLET_MAP = Maps.newHashMap();

    public static final int MAX_RADIUS = 1000;

    private final ServerPlayerEntity player;
    private final BlockPos center;
    private final int radius;
    private final BlockPos fromPos;
    private final BlockPos toPos;

    private final World world;

    private ChunkPos currentPos;

    private boolean finishedTerrain;
    private int tickCounter;

    public TrackingTabletIterator(ServerPlayerEntity player, BlockPos center, int squareRadius) {
        this.player = player;
        this.world = player.level;
        this.center = center;

        squareRadius = Math.min(Math.abs(squareRadius), MAX_RADIUS);
        this.radius = squareRadius;

        this.fromPos = center.offset(-squareRadius, 0, -squareRadius);
        this.toPos = center.offset(squareRadius, 0, squareRadius);

        if(PLAYER_TO_TABLET_MAP.containsKey(player.getUUID())) {
            PLAYER_TO_TABLET_MAP.get(player.getUUID()).finish();
        }

        PLAYER_TO_TABLET_MAP.put(player.getUUID(), this);

        this.currentPos = new ChunkPos(this.fromPos);
        ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(() -> this.player), new S2CStartTrackingTabletHandshake(this.fromPos.getX(), this.toPos.getX(), this.fromPos.getZ(), this.toPos.getZ()));
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        long startTime = System.currentTimeMillis();

        if(!this.finishedTerrain) {
            for (int i = 0; i < 10; i++) {
                this.doCurrentChunk();
                if(!this.incrementPosition()) {
                    this.finishedTerrain = true;
                    break;
                }
                //After 20ms, do no more
                if(System.currentTimeMillis() - startTime >= 20) {
                    break;
                }
            }
        }

        if(this.tickCounter++ % 5 == 0) {
            ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(() -> this.player), new S2CSetTrackingDataList(
                TrackingSavedData.getData((ServerWorld) event.world).getEntries().stream()
                    .filter(entry -> this.center.distSqr(entry.getPosition().x, 0, entry.getPosition().z, true) <= this.radius*this.radius)
                    .collect(Collectors.toList())
            ));
        }
    }

    private Biome[][] getBiomes(int startX, int startZ, int endX, int endZ) {
        Biome[][] biomes = new Biome[endX - startX + 1][endZ - startZ + 1];
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(startX, 0, startZ), new BlockPos(endX, 0, endZ))) {
            biomes[pos.getX() - startX][pos.getZ() - startZ] = this.world.getBiome(pos);
        }
        return biomes;
    }

    private void doCurrentChunk() {
        //All values are inclusive.
        int xStart = MathHelper.clamp(this.currentPos.getMinBlockX(), this.fromPos.getX(), this.toPos.getX());
        int xEnd = MathHelper.clamp(this.currentPos.getMaxBlockX(), this.fromPos.getX(), this.toPos.getX());

        int zStart = MathHelper.clamp(this.currentPos.getMinBlockZ(), this.fromPos.getZ(), this.toPos.getZ());
        int zEnd = MathHelper.clamp(this.currentPos.getMaxBlockZ(), this.fromPos.getZ(), this.toPos.getZ());

        int[] colorData = new int[(xEnd - xStart + 1)*(zEnd - zStart + 1)];
        Biome[][] biomes = this.getBiomes(xStart-1, zStart-1, xEnd + 1, zEnd + 1);

        this.generateBiomeData(xStart, zStart, xEnd, zEnd, colorData, biomes);

        if(this.isChunkOffsetGenerated(0, 0)) {
            this.generateBlockMapData(xStart, zStart, xEnd, zEnd, colorData, biomes);
        }

        ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(() -> this.player), new S2STrackingTabletUpdateChunk(xStart, xEnd, zStart, zEnd, colorData));
    }

    private void generateBlockMapData(int xStart, int zStart, int xEnd, int zEnd, int[] colorData, Biome[][] biomes) {
        int[] biomeData = Arrays.copyOf(colorData, colorData.length);
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int z = zStart; z <= zEnd ; z++) {
            for (int x = xStart; x <= xEnd ; x++) {
                BlockPos.Mutable blockPos = this.getTopBlock(pos.set(x, 0, z));
                BlockState state = this.world.getBlockState(blockPos);

                int colorIndex = 1;
                if(this.world.isLoaded(blockPos.north())) {
                    int prevHeight = blockPos.getY() - this.getTopBlock(blockPos.move(Direction.NORTH)).getY();
                    colorIndex = MathHelper.clamp(prevHeight, -1, 1) + 1;
                }

                int color;
                //todo: config these blocks
                if(state.getBlock() == Blocks.GRASS) {
                    color = BiomeUtils.getGrassColor(biomes[this.index(xStart, x)][this.index(zStart, z)], 0.5F);
                } else if (state.is(BlockTags.LEAVES)) {
                    color = BiomeUtils.getGrassColor(biomes[this.index(xStart, x)][this.index(zStart, z)], 0.3F);
                } else if (state.is(Blocks.WATER)) {
                    color = biomes[this.index(xStart, x)][this.index(zStart, z)].getSpecialEffects().getWaterColor();
                } else {
                    color = state.getMapColor(this.world, blockPos).col;
                }
                colorData[this.getIndex(xStart, xEnd, zStart, x, z)] = this.getMapColor(color, colorIndex);
            }
        }

        boolean topCorner = this.isChunkOffsetGenerated(0, -1);
        boolean leftCorner = this.isChunkOffsetGenerated(-1, 0);
        boolean rightCorner = this.isChunkOffsetGenerated(1, 0);
        boolean downCorner = this.isChunkOffsetGenerated(0, 1);

        boolean[] generatedGrid = {
            leftCorner && this.isChunkOffsetGenerated(-1, -1) && topCorner, topCorner,  topCorner && this.isChunkOffsetGenerated(1, -1) && rightCorner,
            leftCorner,                   /*Self will always be generated*/ true, rightCorner,
            leftCorner && this.isChunkOffsetGenerated(-1, 1) && downCorner, downCorner, downCorner && this.isChunkOffsetGenerated(1, 1) && rightCorner,
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

                int worldStartX = this.currentPos.getMinBlockX() + quadX*8;
                int worldStartZ = this.currentPos.getMinBlockZ() + quadZ*8;

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

    private boolean isChunkOffsetGenerated(int xOff, int zOff) {
        return this.world.hasChunk(this.currentPos.x + xOff, this.currentPos.z + zOff);
    }

    private void generateBiomeData(int xStart, int zStart, int xEnd, int zEnd, int[] colorData, Biome[][] biomes) {
        for (int z = zStart; z <= zEnd ; z++) {
            for (int x = xStart; x <= xEnd; x++) {

                Biome biome = biomes[this.index(xStart, x)][this.index(zStart, z)];

//                boolean bordered = biomes[this.index(xStart, x-1)][this.index(zStart, z)] != biome ||
//                        biomes[this.index(xStart, x+1)][this.index(zStart, z)] != biome ||
//                        biomes[this.index(xStart, x)][this.index(zStart, z-1)] != biome;
//                        biomes[this.index(xStart, x)][this.index(zStart, z+1)] != biome;

                int index = this.getIndex(xStart, xEnd, zStart, x, z);
//                if(bordered) {
//                    colorData[index] = 0;
//                } else {
                    colorData[index] = BiomeUtils.getBiomeColor(biome);
//                }
            }
        }
    }

    private int index(int start, int v) {
        return v - start + 1;
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

    private BlockPos.Mutable getTopBlock(BlockPos.Mutable topPos) {
        topPos.setY(256);
        while(topPos.getY() >= 1) {
            BlockState state = this.world.getBlockState(topPos);
            if(Block.isShapeFullBlock(state.getShape(this.world, topPos)) || state.getMaterial().isLiquid() || state.getBlock() == Blocks.SNOW) {
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
        if(this.currentPos.getMinBlockX() > this.toPos.getX()) {
            this.currentPos = new ChunkPos(this.fromPos.getX() >> 4, this.currentPos.z + 1);
        }

        //Out of bounds to the bottom, so we're done and should return false
        return this.currentPos.getMinBlockZ() <= this.toPos.getZ();
    }

    public void start() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void finish() {
        MinecraftForge.EVENT_BUS.unregister(this);
        PLAYER_TO_TABLET_MAP.remove(this.player.getUUID());
    }

}
