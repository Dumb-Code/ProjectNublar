package net.dumbcode.projectnublar.server.world.structures.structures;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Setter
@Accessors(chain = true)
public class StructureTemplate extends Structure {

    private final NBTTemplate template;
    protected final PlacementSettings settings = new PlacementSettings();

    public StructureTemplate(NBTTemplate template, int children, int weight) {
        super(weight, children);
        this.template = template;
    }

    @Override
    public StructureInstance createInstance(World world, BlockPos pos, Random random) {
        PlacementSettings.Decision decision = this.settings.makeDecision(random);
        BlockPos range = this.template.transformedBlockPosAround(this.template.getRange(), 0, 0, decision);

        return new Instance(
            world,
            pos.add(-Math.abs(range.getX())/2, 0, -Math.abs(range.getZ())/2),
            this, decision,
            Math.abs(range.getX()), Math.abs(range.getZ()));
    }

    public StructureTemplate settings(Consumer<PlacementSettings> consumer) {
        consumer.accept(this.settings);
        return this;
    }

    //Full Settings
    public StructureTemplate fs() {
        this.settings.fillUp();
        return this;
    }

    @Nullable
    @Override
    public BlockPos attemptSize() {
        return this.template.getRange();
    }

    private class Instance extends StructureInstance {

        private final PlacementSettings.Decision decision;

        public Instance(World world, BlockPos position, StructureTemplate template, PlacementSettings.Decision decision, int xSize, int zSize) {
            super(world, position, xSize, zSize, template);
            this.decision = decision;
        }

        @Override
        public void build(Random random, List<DataHandler> handlers, StructureConstants.Decision decision) {
            Biome biome = this.world.getBiome(this.position);
            StructureTemplate.this.template.addBlocksToWorld(
                this.world, this.position,
                this, handlers, decision, this.decision, random,
                (pos, blockInfo) -> new NBTTemplate.BlockInfo(blockInfo.pos, BlockUtils.getBiomeDependantState(blockInfo.blockState, biome), blockInfo.tileentityData),
                2
            );
            for (DataHandler handler : handlers) {
                handler.end(DataHandler.Scope.STRUCTURE);
            }
        }
    }
}
