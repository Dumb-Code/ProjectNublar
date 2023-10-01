//package net.dumbcode.projectnublar.server.network;
//
//import com.google.common.collect.Lists;
//import io.netty.buffer.ByteBuf;
//import net.dumbcode.projectnublar.client.gui.GuiSkeletalProperties;
//import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
//import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
//import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//
//import java.util.List;
//
//public class S12UpdatePoleList implements IMessage {
//
//    private int x;
//    private int y;
//    private int z;
//    private List<SkeletalProperties.Pole> poleList = Lists.newArrayList();
//
//    public S12UpdatePoleList() { }
//
//    public S12UpdatePoleList(SkeletalBuilderBlockEntity builder, List<SkeletalProperties.Pole> poleLists) {
//        this.x = builder.getPos().getX();
//        this.y = builder.getPos().getY();
//        this.z = builder.getPos().getZ();
//        this.poleList = poleLists;
//    }
//
//    @Override
//    public void fromBytes(ByteBuf buf) {
//        x = buf.readInt();
//        y = buf.readInt();
//        z = buf.readInt();
//        poleList.clear();
//        int size = buf.readInt();
//        for (int i = 0; i < size; i++) {
//            poleList.add(new SkeletalProperties.Pole(ByteBufUtils.readUTF8String(buf), PoleFacing.values()[buf.readInt()]));
//        }
//    }
//
//    @Override
//    public void toBytes(ByteBuf buf) {
//        buf.writeInt(x);
//        buf.writeInt(y);
//        buf.writeInt(z);
//        buf.writeInt(poleList.size());
//        for (SkeletalProperties.Pole pole : poleList) {
//            ByteBufUtils.writeUTF8String(buf, pole.getCubeName());
//            buf.writeInt(pole.getFacing().ordinal());
//        }
//    }
//
//    public static class Handler extends WorldModificationsMessageHandler<S12UpdatePoleList, IMessage> {
//        @Override
//        protected void handleMessage(S12UpdatePoleList message, MessageContext ctx, World world, EntityPlayer player) {
//            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
//            TileEntity te = player.world.getTileEntity(pos);
//            if(te instanceof SkeletalBuilderBlockEntity) {
//                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
//                builder.getSkeletalProperties().setPoles(message.poleList);
//            }
//
//
//            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
//            if(screen instanceof GuiSkeletalProperties) {
//                GuiSkeletalProperties gui = (GuiSkeletalProperties)screen;
//                if(gui.getBuilder().getPos().equals(pos)) {
//                    gui.updateList();//Update the gui screen for other players
//                    if(gui.getEditingPole() != null) {
//                        entries: {
//                            for (GuiSkeletalProperties.PoleEntry entry : gui.entries) {
//                                if(entry.getPole().equals(gui.getEditingPole().getPole())) {
//                                    gui.setEditingPole(entry);
//                                    break entries;
//                                }
//                            }
//                            gui.setEditingPole(null);
//                        }
//                    }
//                }
//            }
//
//            pos.release();
//        }
//    }
//}
