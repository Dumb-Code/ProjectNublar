package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;

@Setter
@Getter
public class SolidColorBackground implements TabletBackground {

    public static final String KEY = "solid_color";

    private int color = 0xFFFFFF;

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("color", this.color);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.color = nbt.getInteger("color");
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(this.color);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.color = buf.readInt();
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        Gui.drawRect(x, y, x + width, y + height, this.color | 0xFF000000);
    }
}
