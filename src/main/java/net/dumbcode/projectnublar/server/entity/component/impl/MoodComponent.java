package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.primitives.Floats;
import lombok.Data;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.MoodChangingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.mood.MoodReason;
import net.dumbcode.projectnublar.server.entity.mood.MoodType;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.MoodInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class MoodComponent extends EntityComponent implements TrackingDataComponent {

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> {
            Map<MoodType, MoodChangingEntry> map = new HashMap<>();
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof MoodChangingComponent) {
                    ((MoodChangingComponent) component).applyMoods((reason, amount) ->
                        reason.getMoodTypes().forEach((moodType, modifier) ->
                            map.computeIfAbsent(moodType, MoodChangingEntry::new).add(reason, amount.floatValue() * modifier)
                    ));
                }
            }
            if(!map.isEmpty()) {
                MoodChangingEntry entry = map.values().stream().max(MoodChangingEntry::compareTo).orElseThrow(NullPointerException::new);
                boolean positive = entry.getNumber() > 0;
                return new MoodInformation(
                    positive ? entry.getType().getPositiveTranslationKey() : entry.getType().getNegativeTranslationKey(),
                    IndexedObject.sortIndex(positive ? entry.getPositiveReasons() : entry.getNegativeReasons()).stream().limit(3).collect(Collectors.toList())
                );
            }
            return null;
        });
    }

    @Data
    private static class MoodChangingEntry implements Comparable<MoodChangingEntry> {
        private final MoodType type;

        private float number;

        private final List<IndexedObject<String>> positiveReasons = new ArrayList<>();
        private final List<IndexedObject<String>> negativeReasons = new ArrayList<>();

        @Override
        public int compareTo(MoodChangingEntry o) {
            return Floats.compare(Math.abs(this.number), Math.abs(o.number));
        }

        private void add(MoodReason reason, float amount) {
            this.number += amount;
            (amount < 0 ? this.negativeReasons : this.positiveReasons).add(new IndexedObject<>(reason.getTranslationKey(), Math.abs(amount)));
        }

    }
}
