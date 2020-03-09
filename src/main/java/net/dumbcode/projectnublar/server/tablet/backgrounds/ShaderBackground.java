package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.client.shader.GlslSandboxShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class ShaderBackground implements TabletBackground {

    public static final String KEY = "shader";

    private GlslSandboxShader shader;
    private boolean needsUpdating;
    private String url = "";

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("url", this.url);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.setUrl(nbt.getString("url"));
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.url);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.setUrl(ByteBufUtils.readUTF8String(buf));
    }

    public void setUrl(String url) {
        this.needsUpdating = !this.url.equals(url);
        this.url = url;
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if(this.needsUpdating) {
            this.needsUpdating = false;
            this.dispose();
            this.shader = GlslSandboxShader.createShader(this.url);
        }

        if(this.shader != null) {
            if(this.shader.getScreenWidth() != width || this.shader.getScreenHeight() != height) {
                this.shader.init(width, height);
            }
            this.shader.render(mouseX - x, mouseY - y);
            this.shader.startShader();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            this.shader.endShader();
        }
    }

    @Override
    public void dispose() {
        if(this.shader != null) {
            this.shader.dispose();
        }
    }
}
