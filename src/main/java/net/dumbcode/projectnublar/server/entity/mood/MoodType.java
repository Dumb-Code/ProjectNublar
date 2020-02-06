package net.dumbcode.projectnublar.server.entity.mood;

import lombok.Data;
import lombok.NonNull;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.attributes.ModifiableFieldModifier;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
public class MoodType {
    private final String positiveTranslationKey;
    private final String negativeTranslationKey;

    private final BiConsumer<ComponentAccess, Float> onChange;

    private MoodType(@NonNull String translationKey, BiConsumer<ComponentAccess, Float> onChange) {
        this.positiveTranslationKey = translationKey + ".positive";
        this.negativeTranslationKey = translationKey + ".negative";
        this.onChange = onChange;
    }

    public static MoodTypeBuilder builder() {
        return new MoodTypeBuilder();
    }

    public static class MoodTypeBuilder {
        private String translationKey;
        private BiConsumer<ComponentAccess, Float> onChange = null;

        MoodTypeBuilder() { }

        public MoodTypeBuilder translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public MoodTypeBuilder addChangeCallback(BiConsumer<ComponentAccess, Float> onChange) {
            if(this.onChange == null) {
                this.onChange = onChange;
            } else {
                this.onChange = this.onChange.andThen(onChange);
            }
            return this;
        }

        public <T extends EntityComponent> MoodTypeBuilder addComponentCallback(EntityComponentType<T, ?> type, BiConsumer<T, Float> onChange) {
            return this.addChangeCallback((access, aFloat) -> access.get(type).ifPresent(c -> onChange.accept(c, aFloat)));
        }

        public <T extends EntityComponent> MoodTypeBuilder addFieldModifierCallback(EntityComponentType<T, ?> type, Function<T, ModifiableField> compToField, Function<Float, ModifiableFieldModifier> modifierCreator) {
            return this.addComponentCallback(type, (t, aFloat) -> compToField.apply(t).addModifier(modifierCreator.apply(aFloat)));
        }

        public MoodType build() {
            return new MoodType(this.translationKey, this.onChange == null ? (c, f) -> {} : onChange);
        }
    }
}
