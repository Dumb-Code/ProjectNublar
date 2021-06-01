package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraftforge.event.RegistryEvent;

import java.util.Collection;
import java.util.HashSet;

public class DinosaurBaseBlock extends Block {

    protected StateContainer<Block, BlockState> stateDefinition;

    private static final DinosaurBlockProperty DINOSAUR_PROPERTY = DinosaurBlockProperty.of("dinosaur");

    private static final Collection<DinosaurBaseBlock> BLOCKS_TO_REGISTER = new HashSet<>();

    public DinosaurBaseBlock(Properties properties) {
        super(properties);

        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        if(!ProjectNublar.DINOSAUR_REGISTRY.isEmpty()) {
            builder.add(DINOSAUR_PROPERTY);
            this.createBlockStateDefinition(builder);
        } else {
            BLOCKS_TO_REGISTER.add(this);
        }
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    protected void onDinosaursRegistered() {
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        builder.add(DINOSAUR_PROPERTY);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    public static Dinosaur getDinosaur(BlockState state) {
        return state.getValue(DINOSAUR_PROPERTY);
    }


    @Override
    public StateContainer<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    public static void onDinosaurRegistryFinished(RegistryEvent.Register<Dinosaur> event) {
        for (DinosaurBaseBlock block : BLOCKS_TO_REGISTER) {
            block.onDinosaursRegistered();
        }
        BLOCKS_TO_REGISTER.clear();
    }
}
