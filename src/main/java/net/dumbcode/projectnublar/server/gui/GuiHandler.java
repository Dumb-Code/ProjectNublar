package net.dumbcode.projectnublar.server.gui;

import net.dumbcode.projectnublar.client.gui.GuiSkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    //All IDS should be negative. This is because the tab system takes up all of the positive numbers. Todo: maybe switch this around?
    public static final int SKELETAL_BUILDER_ID = -1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof MachineModuleBlockEntity) {
            return ((MachineModuleBlockEntity)te).createContainer(player, ID);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        switch (ID) {
            case SKELETAL_BUILDER_ID: {
                if(te instanceof SkeletalBuilderBlockEntity) {
                    return new GuiSkeletalBuilder((SkeletalBuilderBlockEntity)te);
                }
                return null;
            }
        }
        if(te instanceof MachineModuleBlockEntity) {
            return ((MachineModuleBlockEntity)te).createScreen(player, ID);
        }
        return null;
    }
}
