package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.BasicEntityInformation;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicEntityInformationComponent extends EntityComponent implements TrackingDataComponent {
    @Override
    public void addTrackingData(Consumer<Supplier<TrackingDataInformation>> consumer) {
        if(this.access instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) this.access;
            consumer.accept(() -> new BasicEntityInformation(entity.getHealth(), entity.getMaxHealth()));
        }
    }
}
