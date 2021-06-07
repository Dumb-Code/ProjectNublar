package net.dumbcode.projectnublar.server.world.structures.structures.template.data;

import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Predicate;

public class DataHandler {

    private final Scope scope;
    private final Predicate<String> predicate;
    private final StateFunction func;

    private BlockState cache;

    public DataHandler(Scope scope, Predicate<String> predicate, StateFunction  func) {
        this.scope = scope;
        this.predicate = predicate;
        this.func = func;
    }

    public BlockState get(String name, World world, BlockPos pos, Random random, StructureConstants.Decision decision) {
        if(this.predicate.test(name)) {
            if(this.cache == null || this.scope == Scope.BLOCK) {
                this.cache = this.func.getState(world, pos, name, random, decision);
            }
            return this.cache;
        }
        return null;
    }

    public void end(Scope scope) {
        if(this.scope == scope) {
            this.cache = null;
        }
    }

    public enum Scope {
        NETWORK, STRUCTURE, BLOCK
    }

    public interface StateFunction {
        BlockState getState(World world, BlockPos pos, String name, Random random, StructureConstants.Decision decision);
    }
}
