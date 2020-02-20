package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.primitives.Floats;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


public class MoodComponent extends EntityComponent implements FinalizableComponent, TrackingDataComponent {

    @Getter
    private final Map<MoodType, MoodChangingEntry> entries = new HashMap<>();

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.entries.clear();
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof MoodChangingComponent) {
                MoodChangingComponent mood = (MoodChangingComponent) component;
                mood.applyMoods((reason, amountSupplier) ->
                    reason.getMoodTypes().forEach((moodType, modifier) ->
                        this.entries.computeIfAbsent(moodType, MoodChangingEntry::new).add(reason, amountSupplier, modifier, mood::isDirty)
                    )
                );
            }
        }
        this.entries.values().forEach(MoodChangingEntry::recompile);
    }

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> {
            if(!this.entries.isEmpty()) {
                MoodChangingEntry entry = this.entries.values().stream().max(MoodChangingEntry::compareNumbers).orElseThrow(NullPointerException::new);
                boolean positive = entry.getNumber() > 0;
                return new MoodInformation(
                    positive ? entry.type.getPositiveTranslationKey() : entry.type.getNegativeTranslationKey(),
                    IndexedObject.sortIndex(positive ? entry.positiveReasons : entry.negativeReasons).stream().limit(3).collect(Collectors.toList())
                );
            }
            return null;
        });
    }

    @RequiredArgsConstructor
    public static class MoodChangingEntry  {
        private final MoodType type;

        private final List<MoodReasonEntry> entries = new ArrayList<>();

        private float number;
        private final List<IndexedObject<String>> positiveReasons = new ArrayList<>();
        private final List<IndexedObject<String>> negativeReasons = new ArrayList<>();

        private int compareNumbers(MoodChangingEntry o) {
            return Floats.compare(Math.abs(this.number), Math.abs(o.number));
        }

        private void recompile() {
            this.number = 0;
            this.positiveReasons.clear();
            this.negativeReasons.clear();

            for (MoodReasonEntry entry : this.entries) {
                float amount = entry.modifier.apply(entry.amountSupplier.get());
                this.number += amount;
                if(amount != 0) {
                    (amount < 0 ? this.negativeReasons : this.positiveReasons).add(new IndexedObject<>(entry.reason.getTranslationKey(), Math.abs(amount)));
                }
            }
        }

        private float getNumber() {
            for (MoodReasonEntry entry : this.entries) {
                if(entry.isDirty.getAsBoolean()) {
                    this.recompile();
                    break;
                }
            }
            return this.number;
        }

        public void runIfDirty(ComponentAccess access) {
            for (MoodReasonEntry entry : this.entries) {
                if(entry.isDirty.getAsBoolean()) {
                    this.recompile();
                    this.type.getOnChange().accept(access, this.number);
                    break;
                }
            }
        }

        private void add(MoodReason reason, Supplier<Float> amountSupplier, UnaryOperator<Float> modifier, BooleanSupplier isDirty) {
            this.entries.add(new MoodReasonEntry(reason, amountSupplier, modifier, isDirty));
        }
    }

    @RequiredArgsConstructor
    private static class MoodReasonEntry {
        private final MoodReason reason;
        private final Supplier<Float> amountSupplier;
        private final UnaryOperator<Float> modifier;
        private final BooleanSupplier isDirty;
    }
}
