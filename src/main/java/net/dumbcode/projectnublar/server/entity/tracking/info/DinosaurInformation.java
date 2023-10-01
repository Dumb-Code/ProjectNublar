package net.dumbcode.projectnublar.server.entity.tracking.info;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

@Value
public class DinosaurInformation extends TooltipInformation {

    public static final String KEY = "dinosaur_info";

    private Dinosaur dinosaur;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    public void renderMap(GuiGraphics stack, int x, int y) {
        String s = this.dinosaur.getRegName().getPath().substring(0, 1);
        int width = Minecraft.getInstance().font.width(s);
        Minecraft.getInstance().stack.drawString(font, s, x - width/2F, y - 3, 0xAAAAAA);
        super.renderMap(stack, x, y);
    }

    @Override
    protected List<String> getTooltipLines() {
        return Collections.singletonList(I18n.get("projectnublar.gui.tracking.dinosaur", I18n.get(this.dinosaur.createNameTranslationKey())));
    }

    public static void encodeNBT(CompoundNBT nbt, DinosaurInformation info) {
        nbt.putString("dinosaur", info.dinosaur.getRegName().toString());
    }

    public static DinosaurInformation decodeNBT(CompoundNBT nbt) {
        return new DinosaurInformation(DinosaurHandler.getRegistry().getValue(new ResourceLocation(nbt.getString("dinosaur"))));
    }

    public static void encodeBuf(PacketBuffer buf, DinosaurInformation info) {
        buf.writeRegistryId(info.dinosaur);
    }

    public static DinosaurInformation decodeBuf(PacketBuffer buf) {
        return new DinosaurInformation(buf.readRegistryIdSafe(Dinosaur.class));
    }
}
