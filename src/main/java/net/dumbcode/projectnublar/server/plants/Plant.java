package net.dumbcode.projectnublar.server.plants;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockstateComponentProperty;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Plant extends IForgeRegistryEntry.Impl<Plant> {
    protected final EntityComponentAttacher baseAttacher = new EntityComponentAttacher();
    protected final Map<String, EntityComponentAttacher> stateOverrides = new HashMap<>();

    public Block createBlock() {
        return new BlockPlant();
    }

    public BlockstateComponentProperty createOverrideProperty() {
        return new BlockstateComponentProperty(this.baseAttacher, this.stateOverrides);
    }

    public void attachComponents() {
    }

    public void attachOverride(String overrideName, Consumer<EntityComponentAttacher> onAttach) {
        EntityComponentAttacher attacher = new EntityComponentAttacher();
        onAttach.accept(attacher);
        this.stateOverrides.put(overrideName, attacher);
    }

    //todo: move to own class
    public class BlockPlant extends Block implements BlockPropertyAccess, IItemBlock {

        private BlockstateComponentProperty property;

        public BlockPlant() {
            super(Material.PLANTS);
        }

        protected BlockstateComponentProperty getProperty() {
            if(this.property == null) {
                this.property = Plant.this.createOverrideProperty();
            }
            return this.property;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, this.getProperty());
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return this.getDefaultState().withProperty(this.property, this.property.toEntry(meta));
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return this.property.toMeta(state.getValue(this.property));
        }

        @Override
        public BlockstateComponentProperty getComponentProperty() {
            return this.property;
        }
    }


}
