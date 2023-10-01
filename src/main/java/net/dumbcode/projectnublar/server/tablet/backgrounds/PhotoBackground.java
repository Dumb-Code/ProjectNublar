package net.dumbcode.projectnublar.server.tablet.backgrounds;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SRequestBackgroundImage;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
                this.texture.close();
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
    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        nbt.putString("uploader_uuid", this.uploaderUUID);
        nbt.putString("photo_hash", this.photoHash);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundNBT nbt) {
        this.setPhoto(nbt.getString("uploader_uuid"), nbt.getString("photo_hash"));
    }

    @Override
    public void writeToBuf(PacketBuffer buf) {
        buf.writeUtf(this.uploaderUUID);
        buf.writeUtf(this.photoHash);
    }

    @Override
    public void readFromBuf(PacketBuffer buf) {
        this.setPhoto(buf.readUtf(), buf.readUtf());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY) {
        if(!this.requested) {
            this.requested = true;
            ProjectNublar.NETWORK.sendToServer(new C2SRequestBackgroundImage(this.uploaderUUID, this.photoHash));
        }

        if(this.texture != null) {
            this.texture.bind();
            AbstractGui.stack.blit(x, y, 0, 0, width, height, width, height);
        }
    }

}
