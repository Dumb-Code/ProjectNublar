package net.dumbcode.projectnublar.server.plants;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockstateComponentProperty;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Plant extends IForgeRegistryEntry.Impl<Plant> {
    protected final EntityComponentAttacher baseAttacher = new EntityComponentAttacher();
    protected final Map<String, Map<String, EntityComponentAttacher>> states = new HashMap<>();

    public Block createBlock() {
        return new BlockPlant();
    }

    public BlockstateComponentProperty[] createOverrideProperty() {
        return this.states.entrySet()
                .stream()
                .map(entry -> new BlockstateComponentProperty(entry.getKey(), this.baseAttacher, entry.getValue()))
                .toArray(BlockstateComponentProperty[]::new);
    }

    public void attachComponents() {
    }

    public StateAttacher attachProperty(String propertyName) {
        return new StateAttacher(this.states.computeIfAbsent(propertyName, s -> new HashMap<>()));
    }



    public JsonObject toJson(JsonObject object) {
        object.add("base_values", this.baseAttacher.writeToJson(new JsonArray()));

        JsonObject overrides = new JsonObject();
        this.states.forEach((name, map) -> {
            JsonObject obj = new JsonObject();
            map.forEach((key, attacher) -> obj.add(key, attacher.writeToJson(new JsonArray())));
            overrides.add(name, obj);
        });
        object.add("overrides", overrides);

        return object;
    }

    @RequiredArgsConstructor
    protected class StateAttacher {
        private final Map<String, EntityComponentAttacher> stateOverrides;

        protected StateAttacher attachOverride(String overrideName, Consumer<EntityComponentAttacher> onAttach) {
            EntityComponentAttacher attacher = new EntityComponentAttacher();
            onAttach.accept(attacher);
            this.stateOverrides.put(overrideName, attacher);
            return this;
        }
    }

    //todo: move to own class
    public class BlockPlant extends Block implements BlockPropertyAccess, IItemBlock {

        private BlockstateComponentProperty[] property;

        public BlockPlant() {
            super(Material.PLANTS);
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, this.getComponentProperties());
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            ArrayList<IBlockState> states = new ArrayList<>(this.getBlockState().getValidStates());
            states.sort(Comparator.comparing(Object::toString));
            return states.get(meta);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            ArrayList<IBlockState> states = new ArrayList<>(this.getBlockState().getValidStates());
            states.sort(Comparator.comparing(Object::toString));
            return states.indexOf(state);
        }

        @Override
        public IProperty<? extends ComponentAccess>[] getComponentProperties() {
            if(this.property == null) {
                this.property = Plant.this.createOverrideProperty();
            }
            return this.property;
        }
    }
}
