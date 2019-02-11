package net.dumbcode.projectnublar.client.render;

import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.utils.PropertyRotation;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;

public class FenceStateMapper extends StateMapperBase {

    private final BlockElectricFencePole pole;

    public FenceStateMapper(BlockElectricFencePole pole) {
        this.pole = pole;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        String part = "lower=true,";
        if(state.getValue(this.pole.INDEX_PROPERTY) == this.pole.getType().getHeight() - 1) {
            PropertyBool pow = BlockElectricFencePole.POWERED_PROPERTY;
            part = "lower=false," + pow.getName() + "=" + pow.getName(state.getValue(pow)) + ",";
        }
        PropertyRotation rot = BlockElectricFencePole.ROTATION_PROPERTY;
        part += rot.getName() + "=" + rot.getName(state.getValue(rot));
        return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), part);
    }
}
