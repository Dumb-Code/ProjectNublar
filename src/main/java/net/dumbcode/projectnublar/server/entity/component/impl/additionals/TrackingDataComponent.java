package net.dumbcode.projectnublar.server.entity.component.impl.additionals;

import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TrackingDataComponent {
    void addTrackingData(Consumer<Supplier<TrackingDataInformation>> consumer);
}
