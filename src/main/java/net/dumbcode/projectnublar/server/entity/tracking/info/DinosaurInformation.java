package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Collections;
import java.util.List;

@Value
public class DinosaurInformation extends TooltipInformation {

    private final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    public static final String KEY = "dinosaur_info";

    private Dinosaur dinosaur;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    public void renderMap(int x, int y) {
        String s = this.dinosaur.getRegName().getPath().substring(0, 1);
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(s, x - width/2, y - 3, 0xAAAAAA);
        super.renderMap(x, y);
    }

    @Override
    protected List<String> getTooltipLines() {
        return Collections.singletonList(I18n.format("projectnublar.gui.tracking.dinosaur", this.dinosaur.createNameComponent().getUnformattedText()));
    }

    public static void encodeNBT(NBTTagCompound nbt, DinosaurInformation info) {
        nbt.setString("dinosaur", info.dinosaur.getRegName().toString());
    }

    public static DinosaurInformation decodeNBT(NBTTagCompound nbt) {
        return new DinosaurInformation(ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("dinosaur"))));
    }

    public static void encodeBuf(ByteBuf buf, DinosaurInformation info) {
        ByteBufUtils.writeRegistryEntry(buf, info.dinosaur);
    }

    public static DinosaurInformation decodeBuf(ByteBuf buf) {
        return new DinosaurInformation(ByteBufUtils.readRegistryEntry(buf, ProjectNublar.DINOSAUR_REGISTRY));
    }
}
