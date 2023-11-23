package net.dumbcode.projectnublar.server.tablet.backgrounds;

import net.dumbcode.projectnublar.client.gui.tablet.setuppages.PhotoBackgroundSetup;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SetupPage;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.ShaderSetupPage;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SolidColorSetupPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.function.Supplier;

public interface TabletBackground {

    String identifier();

    CompoundTag writeToNBT(CompoundTag nbt);
    void readFromNBT(CompoundTag nbt);

    void writeToBuf(FriendlyByteBuf buf);
    void readFromBuf(FriendlyByteBuf buf);

    @OnlyIn(Dist.CLIENT)
    void render(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY);

    default void dispose(){
    }

    HashMap<String, Entry<?>> REGISTRY = new HashMap<>();

    static void registerDefaults() {
        REGISTRY.put(SolidColorBackground.KEY, new Entry<>(SolidColorBackground::new, SolidColorSetupPage::new));
        REGISTRY.put(PhotoBackground.KEY, new Entry<>(PhotoBackground::new, PhotoBackgroundSetup::new));
        REGISTRY.put(ShaderBackground.KEY, new Entry<>(ShaderBackground::new, ShaderSetupPage::new));
    }

    class Entry<T extends TabletBackground>  {
        private final Supplier<T> backgroundSupplier;
        private final Supplier<SetupPage<T>> setupPage;

        public Entry(Supplier<T> backgroundSupplier, Supplier<SetupPage<T>> setupPage) {
            this.backgroundSupplier = backgroundSupplier;
            this.setupPage = setupPage;
        }

        public SetupPage<T> getSetupPage() {
            return this.setupPage.get();
        }

        public T getBackground() {
            return this.backgroundSupplier.get();
        }
    }
}
