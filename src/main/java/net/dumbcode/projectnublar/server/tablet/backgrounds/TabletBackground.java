package net.dumbcode.projectnublar.server.tablet.backgrounds;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.PhotoBackgroundSetup;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SetupPage;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.ShaderSetupPage;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SolidColorSetupPage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.function.Supplier;

public interface TabletBackground {

    String identifier();

    CompoundNBT writeToNBT(CompoundNBT nbt);
    void readFromNBT(CompoundNBT nbt);

    void writeToBuf(PacketBuffer buf);
    void readFromBuf(PacketBuffer buf);

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
