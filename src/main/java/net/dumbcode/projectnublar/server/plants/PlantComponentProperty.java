package net.dumbcode.projectnublar.server.plants;

import com.google.common.base.Optional;
import lombok.Value;
import net.dumbcode.dumblibrary.server.entity.ComponentMapWriteAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentAttacher;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentMap;
import net.minecraft.block.properties.IProperty;

import java.util.*;

/**
 * The blockstate property used to hold the {@link net.dumbcode.dumblibrary.server.entity.ComponentAccess} for the entity.
 * To call {@link net.minecraft.block.state.IBlockState#withProperty(IProperty, Comparable)} with this property, call
 * {@link #getFromString(String)} to get the entry from a string.
 * @author Wyn Price
 */
public class PlantComponentProperty implements IProperty<PlantComponentProperty.Entry> {

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Entry> stringToEntries = new HashMap<>();

    PlantComponentProperty(EntityComponentAttacher baseAttacher, Map<String, EntityComponentAttacher> stateOverrides) {

        if(stateOverrides.isEmpty()) {
            String name = "default";
            EntityComponentMap map = new EntityComponentMap();
            baseAttacher.getDefaultConfig().attachAll(map.getWriteable());

            Entry entry = new Entry(name, map);
            this.entries.add(entry);
            this.stringToEntries.put(name, entry);
        } else {
            stateOverrides.forEach((name, attacher) -> {
                EntityComponentMap map = new EntityComponentMap();
                baseAttacher.getDefaultConfig().attachAll(map.getWriteable());
                attacher.getDefaultConfig().attachAll(map.getWriteable());

                Entry entry = new Entry(name, map);
                this.entries.add(entry);
                this.stringToEntries.put(name, entry);
            });
        }
    }

    @Override
    public String getName() {
        return "components";
    }

    @Override
    public Collection<Entry> getAllowedValues() {
        return this.entries;
    }

    @Override
    public Class<Entry> getValueClass() {
        return Entry.class;
    }

    @Override
    public Optional<Entry> parseValue(String value) {
        return Optional.fromNullable(this.stringToEntries.get(value));
    }

    public Entry getFromString(String value) {
        return this.parseValue(value).or(this.entries.get(0));
    }

    @Override
    public String getName(Entry value) {
        return value.name;
    }


    @Value
    public static class Entry implements ComponentMapWriteAccess, Comparable<Entry> {
        private final String name;
        private final EntityComponentMap componentMap;

        @Override
        public int compareTo(Entry o) {
            return this.name.compareTo(o.name);
        }
    }



}
