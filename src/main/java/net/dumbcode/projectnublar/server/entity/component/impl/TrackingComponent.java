package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class TrackingComponent extends EntityComponent implements FinalizableComponent {

    private final List<Supplier<TrackingDataInformation>> infoSuppliers = new ArrayList<>();

    public List<Supplier<TrackingDataInformation>> getInfoSuppliers() {
        return Collections.unmodifiableList(this.infoSuppliers);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.infoSuppliers.clear();
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof TrackingDataComponent) {
                ((TrackingDataComponent) component).addTrackingData(this.infoSuppliers::add);
            }
        }
    }
}
