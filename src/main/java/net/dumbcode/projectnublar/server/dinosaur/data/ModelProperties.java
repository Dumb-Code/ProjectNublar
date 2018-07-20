package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import net.dumbcode.projectnublar.client.render.dinosaur.objects.EntityAnimator;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Data
public class ModelProperties {
    private List<GrowthStage> modelGrowthStages = Lists.newArrayList(GrowthStage.ADULT);
    private Map<GrowthStage, String> mainModelMap = Maps.newEnumMap(GrowthStage.class);
    private Supplier<EntityAnimator> entityAnimatorSupplier = EntityAnimator::new;
}
