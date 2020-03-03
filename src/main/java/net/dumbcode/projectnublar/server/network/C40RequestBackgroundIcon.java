package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C40RequestBackgroundIcon implements IMessage {

    private boolean global;
    private String uploaderUUID;
    private String imageHash;

    public C40RequestBackgroundIcon() {
    }

    public C40RequestBackgroundIcon(String uploaderUUID, String imageHash, boolean global) {
        this.uploaderUUID = uploaderUUID;
        this.imageHash = imageHash;
        this.global = global;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uploaderUUID = ByteBufUtils.readUTF8String(buf);
        this.imageHash = ByteBufUtils.readUTF8String(buf);
        this.global = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uploaderUUID);
        ByteBufUtils.writeUTF8String(buf, this.imageHash);
        buf.writeBoolean(this.global);
    }

    public static class Handler extends WorldModificationsMessageHandler<C40RequestBackgroundIcon, C40RequestBackgroundIcon> {

        @Override
        protected void handleMessage(C40RequestBackgroundIcon message, MessageContext ctx, World world, EntityPlayer player) {
            TabletBGImageHandler.getFullImage(world, message.uploaderUUID, message.imageHash, true).ifPresent(image ->
                ProjectNublar.NETWORK.sendTo(new S39SyncBackgroundIcon(message.uploaderUUID, message.imageHash, message.global, image), (EntityPlayerMP) player)
            );
        }
    }
}
