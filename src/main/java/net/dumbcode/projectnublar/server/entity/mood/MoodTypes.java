package net.dumbcode.projectnublar.server.entity.mood;

import net.dumbcode.dumblibrary.server.attributes.ModifiableFieldModifier;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;

import java.util.UUID;

public class MoodTypes {

    public static final MoodType STRESSFUL = MoodType.builder()
        .translationKey("projectnublar.mood.stressful")
        .addFieldModifierCallback(ComponentHandler.METABOLISM, MetabolismComponent::getFoodRate, amount ->
            new ModifiableFieldModifier(UUID.fromString("0f5179dc-1ad6-4123-aac9-40d0d0126d7f"), ModOp.MULTIPLY_BASE_THEN_ADD, -amount / 16F) //Adds +/- 0.25 to the food rate
        )
        .addFieldModifierCallback(ComponentHandler.METABOLISM, MetabolismComponent::getWaterRate, amount ->
            new ModifiableFieldModifier(UUID.fromString("05dd8f00-ac20-4890-8ae2-812da9bc22a7"), ModOp.MULTIPLY_BASE_THEN_ADD, -amount / 16F) //Adds +/- 0.25 to the water rate
        )
        .build();

    public static final MoodType HAPPY = MoodType.builder()
        .translationKey("projectnublar.mood.happy")
        .build();

    public static final MoodType AWAKE = MoodType.builder()
        .translationKey("projectnublar.mood.awake")
        .addFieldModifierCallback(EntityComponentTypes.SLEEPING, SleepingComponent::getSleepTime, amount ->
            new ModifiableFieldModifier(UUID.fromString("d7ad52a0-4421-4b32-85d4-b1201d4c012d"), ModOp.MULTIPLY_BASE_THEN_ADD, amount / 8D)
        )
        .build();


}
