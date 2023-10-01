package net.dumbcode.projectnublar.server.block;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.item.MachineModuleBuildPart;
import net.dumbcode.projectnublar.server.item.MachineModulePart;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class UnbuiltMachineModuleBlock extends HorizontalBlock implements IItemBlock {

    private final StateContainer<Block, BlockState> stateDefinition;

    private final Map<String, Pair<MachineModuleBuildPart, BooleanProperty>> propertyMap = new HashMap<>();
    private final Map<Item, Pair<MachineModuleBuildPart, BooleanProperty>> hydratedPropertyMap = new HashMap<>();
    private final Supplier<MachineModuleBlock> fullyBuilt;
    private boolean hydrated;

    public UnbuiltMachineModuleBlock(Supplier<MachineModuleBlock> fullyBuilt, Properties properties, MachineModuleBuildPart... items) {
        super(properties);
        this.fullyBuilt = fullyBuilt;

        Set<String> names = new HashSet<>();
        for (MachineModuleBuildPart part : items) {
            String original = part.getName();
            String name = original;
            int counter = 1;
            while (names.contains(name)) {
                name = original + "_" + counter++;
            }

            names.add(name);
            this.propertyMap.put(name, Pair.of(part, BooleanProperty.create(name)));
        }

        //Needed as the container making is usually created in the block constructor, so the propertyMap values arn't set correctly (the map is null)
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.createBlockStateDefinition(builder);
        builder.add(FACING);
        for (Pair<MachineModuleBuildPart, BooleanProperty> pair : this.propertyMap.values()) {
            builder.add(pair.getSecond());
        }
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);

        BlockState baseState = this.stateDefinition.any();
        for (Pair<MachineModuleBuildPart, BooleanProperty> value : this.propertyMap.values()) {
            baseState = baseState.setValue(value.getSecond(), false);
        }
        this.registerDefaultState(baseState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getHorizontalDirection();
        int rotateAmount = this.fullyBuilt.get().getRotateAmount();
        for (int i = 0; i < rotateAmount; i++) {
            direction = direction.getClockWise();
        }
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public StateContainer<Block, BlockState> getStateDefinition() {
        return stateDefinition;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        this.hydrateIfNeeded();
        Item item = player.getItemInHand(hand).getItem();
        if(this.hydratedPropertyMap.containsKey(item)) {
            Pair<MachineModuleBuildPart, BooleanProperty> pair = this.hydratedPropertyMap.get(item);
            if(!state.getValue(pair.getSecond())) {
                for (String dependency : pair.getFirst().getDependencies()) {
                    if(!state.getValue(this.propertyMap.get(dependency).getSecond())) {
                        return ActionResultType.CONSUME;
                    }
                }
                BlockState newState = state.setValue(pair.getSecond(), true);


                for (Pair<MachineModuleBuildPart, BooleanProperty> p : this.hydratedPropertyMap.values()) {
                    if(!newState.getValue(p.getSecond())) {
                        world.setBlock(pos, newState, 3);
                        return ActionResultType.SUCCESS;
                    }
                }

                world.setBlock(pos, this.fullyBuilt.get().defaultBlockState().setValue(MachineModuleBlock.FACING, state.getValue(FACING)), 3);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.CONSUME;
    }

    private void hydrateIfNeeded() {
        if(!this.hydrated) {
            this.hydrated = true;
            for (Pair<MachineModuleBuildPart, BooleanProperty> value : this.propertyMap.values()) {
                this.hydratedPropertyMap.put(value.getFirst().getItem(), value);
            }
        }
    }
}
