package net.dumbcode.projectnublar.server.world.structures;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.PredicateTraverser;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.StructurePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Getter
public abstract class StructureInstance {

    @Nullable
    protected final StructureInstance parent;
    protected final ServerWorld world;
    protected final BlockPos position;

    protected final int children;

    protected final int xSize;
    protected final int zSize;

    protected final List<StructurePredicate> predicates;

    protected final List<String> globalPredicates = new ArrayList<>();

    protected Boolean cachedBuildResult;

    public StructureInstance(@Nullable StructureInstance parent, ServerWorld world, BlockPos position, int xSize, int zSize, Structure structure, StructurePredicate... predicates) {
        this.parent = parent;
        this.world = world;
        this.children = structure.getChildren();
        this.position = BlockUtils.getTopSolid(world, new BlockPos(position.getX(), 257, position.getZ()));
        this.xSize = xSize;
        this.zSize = zSize;
        this.predicates = Lists.newArrayList(predicates);
        this.globalPredicates.addAll(structure.globalPredicates);
    }

    private boolean applyPredicates() {
        for (StructurePredicate predicate : this.predicates) {
            if(!predicate.canBuildDirect(this)) {
                return false;
            }
        }

        List<PredicateTraverser<?>> traversers = new ArrayList<>();
        for (StructurePredicate predicate : this.predicates) {
            predicate.setupTraversers(traversers::add);
        }
        this.traverseTopdown(pos -> traversers.forEach(t -> t.onTraverse(this, pos)));

        for (PredicateTraverser<?> traverser : traversers) {
            if(!traverser.acceptable()) {
                return false;
            }
        }
        return true;
    }

    public boolean canBuild() {
        if(this.cachedBuildResult == null) {
            return this.cachedBuildResult = this.applyPredicates();
        }
        return this.cachedBuildResult;
    }

    public abstract void build(Random random, List<DataHandler> handlers, StructureConstants.Decision decision);

    protected void traverseTopdown(Consumer<BlockPos> consumer) {
        for (int x = 0; x <= this.xSize; x++) {
            for (int z = 0; z <= this.zSize; z++) {
                consumer.accept(WorldUtils.getDirectTopdownBlock(this.world, this.position.offset(x, 0, z)));
            }
        }

    }
}
