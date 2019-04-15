package net.dumbcode.projectnublar.server.world.structures.structures.template.data;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class DataHandler {

    private final Scope scope;
    private final Predicate<String> predicate;
    private final StateFunction  func;

    private IBlockState cache;

    public DataHandler(Scope scope, Predicate<String> predicate, StateFunction  func) {
        this.scope = scope;
        this.predicate = predicate;
        this.func = func;
    }

    public IBlockState get(String name, World world, BlockPos pos, Random random) {
        if(this.predicate.test(name)) {
            if(this.cache == null || this.scope == Scope.BLOCK) {
                this.cache = this.func.getState(world, pos, name, random);
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
        IBlockState getState(World world, BlockPos pos, String name, Random random);
    }
}
