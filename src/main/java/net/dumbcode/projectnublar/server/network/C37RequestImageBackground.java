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

public class C37RequestImageBackground implements IMessage {

    private String uploaderUUID;
    private String imageHash;

    public C37RequestImageBackground() {
    }

    public C37RequestImageBackground(String uploaderUUID, String imageHash) {
        this.uploaderUUID = uploaderUUID;
        this.imageHash = imageHash;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uploaderUUID = ByteBufUtils.readUTF8String(buf);
        this.imageHash = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uploaderUUID);
        ByteBufUtils.writeUTF8String(buf, this.imageHash);
    }

    public static class Handler extends WorldModificationsMessageHandler<C37RequestImageBackground, C37RequestImageBackground> {

        @Override
        protected void handleMessage(C37RequestImageBackground message, MessageContext ctx, World world, EntityPlayer player) {
            TabletBGImageHandler.getFullImage(world, message.uploaderUUID, message.imageHash).ifPresent(image ->
                ProjectNublar.NETWORK.sendTo(new S38SyncBackgroundImage(image), (EntityPlayerMP) player)
            );
        }
    }
}
