package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface RegistryStackNBTProvider<T extends IForgeRegistryEntry.Impl<T>> extends StackModelVarient<T> {

    IForgeRegistry<T> getRegistry();

    @Override
    default List<T> getAllValues() {
        return Lists.newArrayList(getRegistry().getValuesCollection());
    }

    @Override
    default String getUniqueNameFor(T type) {
        return type.getRegistryName().toString();
    }

    @Nonnull
    @Override
    default T getValueFromName(String string) {
        return getRegistry().getValue(new ResourceLocation(string));
    }

    default Map<Object, ResourceLocation> getModelResourceLocations(T value) {
        ResourceLocation res = ((IForgeRegistryEntry.Impl)this).getRegistryName(); //A bit of a hack, but whatever
        ResourceLocation reg = value.getRegistryName();
        if(reg == null) {
            throw new NullPointerException("Registry name was null");
        }
        Map<Object, ResourceLocation> ret = Maps.newHashMap();
        ret.put(getDefault(), new ResourceLocation(reg.getResourceDomain(), getFolderLocation(res) + "/" + reg.getResourcePath()));
        return ret;
    }

    default String getFolderLocation(ResourceLocation res) {
        return "item/" + res.getResourcePath();
    }

}