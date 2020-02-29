package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.function.Supplier;

public interface TabletBackground {

    String identifier();

    NBTTagCompound writeToNBT(NBTTagCompound nbt);
    void readFromNBT(NBTTagCompound nbt);

    void writeToBuf(ByteBuf buf);
    void readFromBuf(ByteBuf buf);

    void render(int x, int y, int width, int height, int mouseX, int mouseY);

    HashMap<String, Entry<?>> REGISTRY = new HashMap<>();

    static void registerDefaults() {
        REGISTRY.put(SolidColorBackground.KEY, new Entry<>(SolidColorBackground::new, SolidColorBackground.SolidColorSetupPage::new));
    }

    @Value class Entry<T extends TabletBackground> { Supplier<T> backgroundSupplier; Supplier<SetupPage<T>> setupPage; }
}
