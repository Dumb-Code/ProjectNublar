package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundTabletScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.PhotoBackgroundSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2CSyncBackgroundIcon {

    private final String uploaderUUID;
    private final String imageHash;
    private final boolean global;
    private final byte[] data;

    public static S2CSyncBackgroundIcon fromBytes(FriendlyByteBuf buf) {
        return new S2CSyncBackgroundIcon(
            buf.readUtf(),
            buf.readUtf(),
            buf.readBoolean(),
            buf.readByteArray(buf.readInt())
        );
    }

    public static void toBytes(S2CSyncBackgroundIcon packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.uploaderUUID);
        buf.writeUtf(packet.imageHash);
        buf.writeBoolean(packet.global);
        buf.writeInt(packet.data.length);
        buf.writeByteArray(packet.data);
    }

    public static void handle(S2CSyncBackgroundIcon packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof BackgroundTabletScreen) {
                BackgroundTabletScreen tabletScreen = (BackgroundTabletScreen) screen;
                if(tabletScreen.getSetupPage() instanceof PhotoBackgroundSetup) {
                    NativeImage read = null;
                    try {
                        read = NativeImage.read(new ByteArrayInputStream(packet.data));
                    } catch (IOException e) {
                        ProjectNublar.LOGGER.error("Unable to read image", e);
                    }
                    ((PhotoBackgroundSetup) tabletScreen.getSetupPage()).loadIcon(packet.uploaderUUID, packet.imageHash, packet.global, read);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
