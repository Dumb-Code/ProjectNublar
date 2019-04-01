package net.dumbcode.projectnublar.server.world.structures.structures;

import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;

import java.util.Random;
import java.util.function.Function;

public class StructureTemplate extends Structure {

    private final NBTTemplate template;

    private final int children;


    public StructureTemplate(NBTTemplate template, int children, int weight) {
        super(weight);
        this.template = template;
        this.children = children;
    }


    @Override
    public StructureInstance createInstance(World world, BlockPos pos, Random random) {
        return new Instance(world, pos, this.children, this.template.getSize().getX(), this.template.getSize().getZ());
    }

    private class Instance extends StructureInstance {

        public Instance(World world, BlockPos position, int children, int xSize, int zSize) {
            super(world, position, children, xSize, zSize);
        }

        @Override
        public void build(Random random) {
            StructureTemplate.this.template.addBlocksToWorld(this.world, this.position, pos -> pos, (pos, blockInfo) -> blockInfo, new PlacementSettings().setRotation(Rotation.values()[random.nextInt(Rotation.values().length)]), 2);
        }
    }
}
