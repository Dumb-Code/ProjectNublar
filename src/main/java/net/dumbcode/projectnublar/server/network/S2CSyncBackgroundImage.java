package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundableScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2CSyncBackgroundImage {

    private final byte[] data;

    public static S2CSyncBackgroundImage fromBytes(FriendlyByteBuf buf) {
        return new S2CSyncBackgroundImage(buf.readByteArray(buf.readInt()));
    }

    public static void toBytes(S2CSyncBackgroundImage packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.data.length);
        buf.writeByteArray(packet.data);
    }

    public static void handle(S2CSyncBackgroundImage packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof BackgroundableScreen) {
                NativeImage image = null;
                try {
                    image = NativeImage.read(new ByteArrayInputStream(packet.data));
                } catch (IOException e) {
                    ProjectNublar.LOGGER.error("Unable to load image", e);
                }

                BackgroundableScreen bgs = (BackgroundableScreen) screen;
                if(!(bgs.getBackground() instanceof PhotoBackground)) {
                    bgs.setBackground(new PhotoBackground());
                }
                ((PhotoBackground)bgs.getBackground()).setTexture(new DynamicTexture(image));
            }
        });

        context.setPacketHandled(true);
    }
}
