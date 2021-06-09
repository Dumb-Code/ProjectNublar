package net.dumbcode.projectnublar.server.plants;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockstateComponentProperty;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Plant extends ForgeRegistryEntry<Plant> {
    protected final EntityComponentAttacher baseAttacher = new EntityComponentAttacher();
    protected final Map<String, Map<String, EntityComponentAttacher>> states = new HashMap<>();

    public Block createBlock() {
        return new BlockPlant(AbstractBlock.Properties.of(Material.PLANT), this.createOverrideProperty());
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
    protected static class StateAttacher {
        private final Map<String, EntityComponentAttacher> stateOverrides;

        protected StateAttacher attachOverride(String overrideName, Consumer<EntityComponentAttacher> onAttach) {
            EntityComponentAttacher attacher = new EntityComponentAttacher();
            onAttach.accept(attacher);
            this.stateOverrides.put(overrideName, attacher);
            return this;
        }
    }

    //todo: move to own class
    public static class BlockPlant extends Block implements BlockPropertyAccess, IItemBlock {

        protected final StateContainer<Block, BlockState> stateDefinition;
        private final BlockstateComponentProperty[] properties;

        public BlockPlant(Properties p_i48440_1_, BlockstateComponentProperty[] properties) {
            super(p_i48440_1_);
            this.properties = properties;
            StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
            builder.add(properties);
            this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
            this.registerDefaultState(this.stateDefinition.any());
        }

        @Override
        public StateContainer<Block, BlockState> getStateDefinition() {
            return this.stateDefinition;
        }

        @Override
        public Property<? extends ComponentAccess>[] getComponentProperties() {
            return this.properties;
        }
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Plant plant : PlantHandler.getRegistry()) {
            event.getRegistry().register(plant.createBlock().setRegistryName(plant.getRegistryName()));
        }
    }
}
