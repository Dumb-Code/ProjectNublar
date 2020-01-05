package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.function.Consumer;

@Value
public class DinosaurInformation extends TrackingDataInformation {

    public static final String KEY = "dinosaur_info";

    private Dinosaur dinosaur;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    public void render(int x, int y) {
        String s = this.dinosaur.getRegName().getPath().substring(0, 1);
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(s, x - width/2, y - 3, 0xAAAAAA);
        super.render(x, y);
    }

    @Override
    public void addTooltip(Consumer<String> lineAdder) {
        lineAdder.accept(I18n.format("projectnublar.gui.tracking.dinosaur", this.dinosaur.getRegName()));
        super.addTooltip(lineAdder);
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
