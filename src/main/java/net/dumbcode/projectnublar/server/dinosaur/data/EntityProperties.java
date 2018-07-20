package net.dumbcode.projectnublar.server.dinosaur.data;

import lombok.Data;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class EntityProperties {
    private Function<World, DinosaurEntity> entityCreateFunction = DinosaurEntity::new;
}
