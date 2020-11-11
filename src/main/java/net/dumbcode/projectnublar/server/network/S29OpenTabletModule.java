package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.function.Consumer;

public class S29OpenTabletModule implements IMessage {

    private TabletModuleType<?> module;
    private Consumer<ByteBuf> bufConsumerWrite;

    private TabletScreen screen;
    private String route;
    
    public S29OpenTabletModule() {
    }

    public S29OpenTabletModule(TabletModuleType<?> module, String route, Consumer<ByteBuf> bufConsumerWrite) {
        this.module = module;
        this.route = route;
        this.bufConsumerWrite = bufConsumerWrite;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	this.route = ByteBufUtils.readUTF8String(buf);
        this.screen = ByteBufUtils.readRegistryEntry(buf, ProjectNublar.TABLET_MODULES_REGISTRY).getScreenCreator().apply(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeUTF8String(buf, this.route);
        ByteBufUtils.writeRegistryEntry(buf, this.module);
        this.bufConsumerWrite.accept(buf);
    }

    public static class Handler extends WorldModificationsMessageHandler<S29OpenTabletModule, S29OpenTabletModule> {

        @Override
        protected void handleMessage(S29OpenTabletModule message, MessageContext ctx, World world, EntityPlayer player) {
            if(message.screen != null) {
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if(screen instanceof OpenedTabletScreen) {
                    message.screen.onSetAsCurrentScreen();
                    ((OpenedTabletScreen) screen).setScreen(message.screen, message.route);
                }
            }
        }
    }
}
