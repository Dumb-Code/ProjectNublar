package net.dumbcode.projectnublar.server.tablet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.server.tablet.backgrounds.SolidColorBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
        NBTTagCompound nbt = this.stack.getOrCreateSubCompound(ProjectNublar.MODID);
        for (NBTBase module : nbt.getTagList("installed_modules", Constants.NBT.TAG_COMPOUND)) {
            String identifier = ((NBTTagCompound) module).getString("identifier");
            TabletModuleType<?> value = ProjectNublar.TABLET_MODULES_REGISTRY.getValue(new ResourceLocation(identifier));
            if(value != null) {
                TabletModuleStorage storage = value.getStorageCreator().get();
                if(storage != null) {
                    storage.readFromNBT(((NBTTagCompound) module).getCompoundTag("storage"));
                }
                this.entryList.add(new Entry(value, storage));
            } else {
                ProjectNublar.getLogger().warn("Unable to find module with identifier {}", identifier);
            }
        }

        NBTTagCompound backgroundNBT = nbt.getCompoundTag("background");
        TabletBackground.Entry<?> entry = TabletBackground.REGISTRY.get(backgroundNBT.getString("identifier"));
        if(entry != null) {
            this.background = entry.getBackgroundSupplier().get();
            this.background.readFromNBT(backgroundNBT.getCompoundTag("storage"));
        }
    }

    public void write() {
        NBTTagCompound nbt = this.stack.getOrCreateSubCompound(ProjectNublar.MODID);

        NBTTagList list = new NBTTagList();
        for (Entry entry : this.entryList) {
            NBTTagCompound entryNBT = new NBTTagCompound();
            entryNBT.setString("identifier", Objects.requireNonNull(entry.type.getRegistryName()).toString());
            if(entry.getStorage() != null) {
                entryNBT.setTag("storage", entry.getStorage().writeToNBT());
            }
            list.appendTag(entryNBT);
        }
        nbt.setTag("installed_modules", list);

        NBTTagCompound backgroundNBT = new NBTTagCompound();
        backgroundNBT.setString("identifier", this.background.identifier());
        backgroundNBT.setTag("storage", this.background.writeToNBT(new NBTTagCompound()));
        nbt.setTag("background", backgroundNBT);
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

        public Consumer<ByteBuf> onOpenScreen(EntityPlayerMP player) {
            return buf -> this.type.getScreenData().accept(this.storage, player, buf);
        }
    }
}
