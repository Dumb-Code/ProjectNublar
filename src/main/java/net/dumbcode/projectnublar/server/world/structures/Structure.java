package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@Accessors(chain = true)
public abstract class Structure implements IResourceManagerReloadListener {

    private final int weight;
    private final int children;
    protected final List<String> globalPredicates = new ArrayList<>();

    protected Structure(int weight, int children) {
        this.weight = weight <= 0 ? 1 : weight;
        this.children = children;
    }

    public Structure addGlobalPredicates(String... globals) {
        Collections.addAll(this.globalPredicates, globals);
        return this;
    }

    public abstract StructureInstance createInstance(World world, BlockPos pos, Random random);

    @Nullable
    public BlockPos attemptSize() {
        return null;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

}
