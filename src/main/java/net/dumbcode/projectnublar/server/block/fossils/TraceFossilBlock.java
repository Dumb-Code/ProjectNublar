package net.dumbcode.projectnublar.server.block.fossils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class TraceFossilBlock extends FossilBlockNeus {

    public static final PropertyEnum<Texture> TEXTURE = PropertyEnum.create("texture", Texture.class);

    public TraceFossilBlock(Background background) {
        super(Type.TRACE, background);
        this.setDefaultState(this.getDefaultState().withProperty(TEXTURE, Texture.AMMONITE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONDITION, TEXTURE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int size = Texture.values().length;
        return state.getValue(CONDITION).ordinal()*size + state.getValue(TEXTURE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int size = Texture.values().length;
        return this.getDefaultState()
            .withProperty(CONDITION, Condition.values()[Math.floorDiv(meta, size)])
            .withProperty(TEXTURE, Texture.values()[Math.floorMod(meta, size)]);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Texture implements IStringSerializable {
        SHELL("shell"), FOOTPRINT("footprint"), AMMONITE("ammonite"),
        FISH("fish"), TOOTH("tooth");

        private final String name;
    }
}
