package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.lwjgl.opengl.GL11;

@Getter
@Setter
public class PhotoBackground implements TabletBackground {

    public static final String KEY = "photo";

    private String photoHash = "";
    private ResourceLocation texture;
    private boolean requested;

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("photo_hash", this.photoHash);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.photoHash = nbt.getString("photo_hash");
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.photoHash);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.photoHash = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if(!this.requested) {
            //Do request ect
        }

        if(this.texture != null) {
            Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        }
    }
}
