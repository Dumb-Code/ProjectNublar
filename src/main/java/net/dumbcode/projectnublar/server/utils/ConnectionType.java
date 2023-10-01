package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ConnectionType {
    Map<ResourceLocation, ConnectionType> registryMap = Maps.newHashMap(); //todo: move to a registry?
    default void register() {
        registryMap.put(this.getRegistryName(), this);
    }
    double[] getOffsets();
    int getHeight();
    float getRadius();
    float getCableWidth();
    float getRotationOffset();
    float getHalfSize();
    int getLightLevel();
    ResourceLocation getRegistryName();

    static ConnectionType getType(ResourceLocation id) {
        return ConnectionType.registryMap.getOrDefault(id, EnumConnectionType.LOW_SECURITY);
    }
}