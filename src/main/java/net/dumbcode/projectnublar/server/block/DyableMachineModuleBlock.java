package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.DyableBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.MachineModulePart;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class DyableMachineModuleBlock extends MachineModuleBlock {

    public DyableMachineModuleBlock(Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier, int rotateAmount, MachineModulePart[] values, Properties properties) {
        super(machineSupplier, rotateAmount, values, properties);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        TileEntity entity = world.getBlockEntity(pos);
        if(item instanceof DyeItem && entity instanceof DyableBlockEntity) {
            DyeColor color = ((DyeItem) item).getDyeColor();
            if(((DyableBlockEntity) entity).getDye() != color) {
                ((DyableBlockEntity) entity).setDye(color);
                entity.setChanged();
                world.sendBlockUpdated(pos, state, state, 3);
                if(!player.isCreative()) {
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return super.use(state, world, pos, player, hand, ray);
    }

}
