package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class S32SetTrackingDataList implements IMessage {

    private final List<TrackingSavedData.DataEntry> entries = new ArrayList<>();

    public S32SetTrackingDataList() {
    }

    public S32SetTrackingDataList(List<TrackingSavedData.DataEntry> list) {
        this.entries.addAll(list);
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        this.entries.clear();
        IntStream.range(0, buf.readShort()).mapToObj(i -> TrackingSavedData.DataEntry.deserailize(buf)).forEach(this.entries::add);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.entries.size());
        this.entries.forEach(entry -> TrackingSavedData.DataEntry.serialize(buf, entry));
    }

    public static class Handler extends WorldModificationsMessageHandler<S32SetTrackingDataList, S32SetTrackingDataList> {

        @Override
        protected void handleMessage(S32SetTrackingDataList message, MessageContext ctx, World world, EntityPlayer player) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof OpenedTabletScreen) {
                TabletPage tabletScreen = ((OpenedTabletScreen) screen).getScreen();
                if(tabletScreen instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletScreen).setTrackingData(message.entries);
                }
            }
        }
    }
}
