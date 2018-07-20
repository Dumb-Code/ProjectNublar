package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ModelProperties {
    private List<GrowthStage> modelGrowthStages = Lists.newArrayList(GrowthStage.ADULT);
    private Map<GrowthStage, String> mainModelMap = Maps.newEnumMap(GrowthStage.class);
}
