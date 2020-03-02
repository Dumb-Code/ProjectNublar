package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C37RequestImageBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

@Getter
@Setter
public class PhotoBackground implements TabletBackground {

    public static final String KEY = "photo";

    private String uploaderUUID = "";
    private String photoHash = "";
    private DynamicTexture texture;
    private boolean requested;

    public void setPhoto(String uploaderUUID, String photoHash) {
        if(!this.uploaderUUID.equals(uploaderUUID) || !this.photoHash.equals(photoHash)) {
            if(this.texture != null) {
                this.texture.deleteGlTexture();
            }
            this.texture = null;
            this.requested = false;
        }
        this.uploaderUUID = uploaderUUID;
        this.photoHash = photoHash;
    }

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("uploader_uuid", this.uploaderUUID);
        nbt.setString("photo_hash", this.photoHash);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.setPhoto(nbt.getString("uploader_uuid"), nbt.getString("photo_hash"));
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uploaderUUID);
        ByteBufUtils.writeUTF8String(buf, this.photoHash);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.setPhoto(ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if(!this.requested) {
            this.requested = true;
            ProjectNublar.NETWORK.sendToServer(new C37RequestImageBackground(this.uploaderUUID, this.photoHash));
        }

        if(this.texture != null) {
            GlStateManager.bindTexture(this.texture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        }
    }
}
