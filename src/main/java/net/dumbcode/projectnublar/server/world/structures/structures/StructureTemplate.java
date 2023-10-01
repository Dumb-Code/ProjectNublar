package net.dumbcode.projectnublar.server.world.structures.structures;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Setter
@Accessors(chain = true)
public class StructureTemplate extends Structure {

    private final NBTTemplate template;
    protected final PnPlacementSettings settings = new PnPlacementSettings();

    public StructureTemplate(NBTTemplate template, int children, int weight) {
        super(weight, children);
        this.template = template;
    }

    @Override
    public StructureInstance createInstance(@Nullable StructureInstance parent, ServerWorld world, BlockPos pos, Random random) {
        PnPlacementSettings.Decision decision = this.settings.makeDecision(random);
        BlockPos range = this.template.transformedBlockPosAround(this.template.getRange(), 0, 0, decision);

        return new Instance(
            parent,
            world,
            pos.offset(-Math.abs(range.getX())/2, 0, -Math.abs(range.getZ())/2),
            this, decision,
            Math.abs(range.getX()), Math.abs(range.getZ()));
    }

    public StructureTemplate settings(Consumer<PnPlacementSettings> consumer) {
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

        private final PnPlacementSettings.Decision decision;

        public Instance(@Nullable StructureInstance parent, ServerWorld world, BlockPos position, StructureTemplate template, PnPlacementSettings.Decision decision, int xSize, int zSize) {
            super(parent, world, position, xSize, zSize, template);
            this.decision = decision;
        }

        @Override
        public void build(Random random, List<DataHandler> handlers, StructureConstants.Decision decision) {
            Biome biome = this.world.getBiome(this.position);
            StructureTemplate.this.template.addBlocksToWorld(
                this.world, this.position,
                this, handlers, decision, this.decision, random,
                (pos, blockInfo) -> new Template.BlockInfo(blockInfo.pos, BlockUtils.getBiomeDependantState(blockInfo.state, biome), blockInfo.nbt),
                2
            );
            for (DataHandler handler : handlers) {
                handler.end(DataHandler.Scope.STRUCTURE);
            }
        }
    }
}
