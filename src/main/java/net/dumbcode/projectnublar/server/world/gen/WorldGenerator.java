//package net.dumbcode.projectnublar.server.world.gen;
//
//import com.google.common.collect.Lists;
//import net.dumbcode.projectnublar.server.ProjectNublar;
//import net.dumbcode.projectnublar.server.block.FossilBlock;
//import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.chunk.IChunkProvider;
//import net.minecraft.world.gen.IChunkGenerator;
//import net.minecraft.world.gen.feature.WorldGenMinable;
//import net.minecraftforge.common.BiomeDictionary;
//import net.minecraftforge.fml.common.IWorldGenerator;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//public enum WorldGenerator implements IWorldGenerator {
//
//    INSTANCE;
//
//    @Override
//    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
//        if(world.provider.getDimension() == 0) {
//            this.generateOverworld(world, random, chunkX * 16, chunkZ * 16);
//        }
//    }
//
//    public void generateOverworld(World world, Random random, int chunkX, int chunkZ) {
//        Biome biome = world.getBiome(new BlockPos(chunkX, 0, chunkZ));
//        int posX = chunkX + random.nextInt(16);
//        int posZ = chunkZ + random.nextInt(16);
//
//        this.generateDinosaurFossils(world, biome, posX, posZ, random);
//
//        this.generateDigsiteStructure(world, posX, posZ, random);
//
//    }
//
//    private void generateDinosaurFossils(World world, Biome biome, int posX, int posZ, Random random) {
//        for (int i = 0; i < 32; i++) {
//            List<Dinosaur> dinoList = Lists.newArrayList(ProjectNublar.DINOSAUR_REGISTRY);
//            Collections.shuffle(dinoList, random);
//            for (Dinosaur dinosaur : dinoList) {
//                for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
//                    if(dinosaur.getDinosaurInfomation().getBiomeTypes().contains(type)) {
//                        int posY = dinosaur.getDinosaurInfomation().getPeriod().getYLevel(random);
//                        new WorldGenMinable(FossilBlock.FossilType.guess(world.getBlockState(new BlockPos(posX, posY, posZ)), dinosaur), 5).generate(world, random, new BlockPos(posX, posY, posZ));
//                        return;
//                    }
//                }
//            }
//        }
//    }
//
//    private void generateDigsiteStructure(World world, int x, int z, Random random) {
//        if (random.nextFloat() < 0.0005) {
//            DigsiteStructureNetwork.NETWORK.generate(world, new BlockPos(x, 100, z), random);
//        }
//    }
//}
