package net.dumbcode.projectnublar.server.gui;

import net.dumbcode.projectnublar.client.gui.GuiSkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int SKELETAL_BUILDER_ID = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case SKELETAL_BUILDER_ID: {
                BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(x, y, z);
                TileEntity te = world.getTileEntity(pos);
                if(te instanceof BlockEntitySkeletalBuilder) {
                    return new GuiSkeletalBuilder((BlockEntitySkeletalBuilder)te);
                }
                return null;
            }
        }
        return null;
    }
}
