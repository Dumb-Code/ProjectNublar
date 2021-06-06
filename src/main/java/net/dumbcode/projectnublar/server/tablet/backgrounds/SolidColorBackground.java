package net.dumbcode.projectnublar.server.tablet.backgrounds;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        nbt.putInt("color", this.color);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundNBT nbt) {
        this.color = nbt.getInt("color");
    }

    @Override
    public void writeToBuf(PacketBuffer buf) {
        buf.writeInt(this.color);
    }

    @Override
    public void readFromBuf(PacketBuffer buf) {
        this.color = buf.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY) {
        AbstractGui.fill(stack, x, y, x + width, y + height, this.color | 0xFF000000);
    }
}
