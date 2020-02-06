package net.dumbcode.projectnublar.server.entity.component.impl.additionals;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TrackingDataComponent {
    void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer);
}
