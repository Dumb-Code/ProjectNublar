package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.PhotoBackgroundSetup;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.SetupPage;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.SolidColorSetupPage;
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
        REGISTRY.put(SolidColorBackground.KEY, new Entry<>(SolidColorBackground::new, SolidColorSetupPage::new));
        REGISTRY.put(PhotoBackground.KEY, new Entry<>(PhotoBackground::new, PhotoBackgroundSetup::new));
    }

    @RequiredArgsConstructor
    class Entry<T extends TabletBackground>  {
        private final Supplier<T> backgroundSupplier;
        private final Supplier<SetupPage<T>> setupPage;

        public SetupPage<T> getSetupPage() {
            return this.setupPage.get();
        }

        public T getBackground() {
            return this.backgroundSupplier.get();
        }
    }
}
