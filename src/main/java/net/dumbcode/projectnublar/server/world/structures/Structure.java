package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@Getter
public abstract class Structure implements IResourceManagerReloadListener {

    private final int weight;

    protected Structure(int weight) {
        this.weight = weight;
    }

    public abstract StructureInstance createInstance(World world, BlockPos pos, Random random);


    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

}
