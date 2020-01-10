package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.MoodChangingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.MoodInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class MoodComponent extends EntityComponent implements TrackingDataComponent {

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> {
            List<MoodInformation.MoodReason> reasons = new ArrayList<>();
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof MoodChangingComponent) {
                    ((MoodChangingComponent) component).applyMoodModifiers((change, reason) -> reasons.add(new MoodInformation.MoodReason(change, reason)));
                }
            }
            if(!reasons.isEmpty()) {
                return new MoodInformation(reasons);
            }
            return null;
        });
    }
}
