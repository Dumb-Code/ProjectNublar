package net.dumbcode.projectnublar.server.gui;

import io.netty.channel.embedded.EmbeddedChannel;
import net.dumbcode.projectnublar.client.gui.GuiSkeletalBuilder;
import net.dumbcode.projectnublar.client.gui.tab.TabListInformation;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.network.S19SetGuiWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public enum GuiHandler implements IGuiHandler {
    INSTANCE;


    //All IDS should be negative. This is because the tab system takes up all of the positive numbers. Todo: maybe switch this around?
    public static final int SKELETAL_BUILDER_ID = -1;

    @Nullable
    @Override
    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
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
            TabListInformation info;
            if(Minecraft.getMinecraft().currentScreen instanceof TabbedGui) {
                info = ((TabbedGui) Minecraft.getMinecraft().currentScreen).getInfo();
            } else {
                info = ((MachineModuleBlockEntity) te).createInfo();
            }
            return ((MachineModuleBlockEntity)te).createScreen(player, info, ID);
        }
        return null;
    }

    public void openAndSyncContainer(int ID, EntityPlayerMP player, World world, int x, int y, int z) {
        Container container = this.getServerGuiElement(ID, player, world, x, y, z);
        if (container != null) {
            player.getNextWindowId();
            player.closeContainer();
            int windowId = player.currentWindowId;
            player.openContainer = container;
            player.openContainer.windowId = windowId;
            player.openContainer.addListener(player);
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
            ProjectNublar.NETWORK.sendTo(new S19SetGuiWindow(windowId), player);
        }
    }
}
