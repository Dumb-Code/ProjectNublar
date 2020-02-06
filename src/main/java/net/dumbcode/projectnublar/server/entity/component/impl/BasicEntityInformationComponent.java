package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.BasicEntityInformation;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicEntityInformationComponent extends EntityComponent implements TrackingDataComponent {
    @Override
    public void addTrackingData(ComponentAccess access, Consumer<Supplier<TrackingDataInformation>> consumer) {
        if(access instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) access;
            consumer.accept(() -> new BasicEntityInformation(entity.getHealth(), entity.getMaxHealth()));
        }
    }
}
