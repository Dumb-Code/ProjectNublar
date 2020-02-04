package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.SleepInformation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SleepTrackingComponent extends EntityComponent implements FinalizableComponent, TrackingDataComponent {

    private SleepingComponent component;

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.component = entity.getOrExcept(EntityComponentTypes.SLEEPING);
    }

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> new SleepInformation(this.component.getTiredness(), Math.round((1 - Math.pow((1 - this.component.calculateChanceToSleep()), 20D*60D)) * 100D) / 100D));
    }
}
