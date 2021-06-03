package net.dumbcode.projectnublar.server.tablet;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.backgrounds.SolidColorBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@Setter
public class TabletItemStackHandler implements AutoCloseable {

    private final ItemStack stack;

    private TabletBackground background = new SolidColorBackground();
    private final List<Entry<?>> entryList = new ArrayList<>();

    public TabletItemStackHandler(ItemStack stack) {
        this.stack = stack;
        this.read();
    }

    @SuppressWarnings("unchecked")
    public void read() {
        this.entryList.clear();
        CompoundNBT nbt = this.stack.getOrCreateTagElement(ProjectNublar.MODID);
        for (INBT module : nbt.getList("installed_modules", Constants.NBT.TAG_COMPOUND)) {
            String identifier = ((CompoundNBT) module).getString("identifier");
            TabletModuleType<?> value = TabletModuleHandler.REGISTRY.get().getValue(new ResourceLocation(identifier));
            if(value != null) {
                TabletModuleStorage storage = value.getStorageCreator().get();
                if(storage != null) {
                    storage.readFromNBT(((CompoundNBT) module).getCompound("storage"));
                }
                this.entryList.add(new Entry(value, storage));
            } else {
                ProjectNublar.getLogger().warn("Unable to find module with identifier {}", identifier);
            }
        }

        CompoundNBT backgroundNBT = nbt.getCompound("background");
        TabletBackground.Entry<?> entry = TabletBackground.REGISTRY.get(backgroundNBT.getString("identifier"));
        if(entry != null) {
            this.background = entry.getBackground();
            this.background.readFromNBT(backgroundNBT.getCompound("storage"));
        }
    }

    public void write() {
        CompoundNBT nbt = this.stack.getOrCreateTagElement(ProjectNublar.MODID);

        ListNBT list = new ListNBT();
        for (Entry entry : this.entryList) {
            CompoundNBT entryNBT = new CompoundNBT();
            entryNBT.putString("identifier", Objects.requireNonNull(entry.type.getRegistryName()).toString());
            if(entry.getStorage() != null) {
                entryNBT.put("storage", entry.getStorage().writeToNBT());
            }
            list.add(entryNBT);
        }
        nbt.put("installed_modules", list);

        CompoundNBT backgroundNBT = new CompoundNBT();
        backgroundNBT.putString("identifier", this.background.identifier());
        backgroundNBT.put("storage", this.background.writeToNBT(new CompoundNBT()));
        nbt.put("background", backgroundNBT);
    }

    public <S extends TabletModuleStorage> void addNew(TabletModuleType<S> type) {
        this.entryList.add(new Entry<>(type, type.getStorageCreator().get()));
    }

    @Override
    public void close() {
        this.write();
    }

    @Value
    public static class Entry<S extends TabletModuleStorage> {
        TabletModuleType<S> type;
        S storage;

        public Consumer<PacketBuffer> onOpenScreen(ServerPlayerEntity player) {
            return buf -> this.type.getScreenData().accept(this.storage, player, buf);
        }
    }
}
