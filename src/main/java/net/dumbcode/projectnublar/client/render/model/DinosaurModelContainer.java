package net.dumbcode.projectnublar.client.render.model;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.val;
import net.dumbcode.projectnublar.client.render.dinosaur.PoseHandler;
import net.dumbcode.projectnublar.client.render.dinosaur.TabulaUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;
import java.util.Map;

@Data
public class DinosaurModelContainer {

    private final Map<GrowthStage, TabulaModel> modelMap = Maps.newEnumMap(GrowthStage.class);
    private final PoseHandler poseHandler;

    public DinosaurModelContainer(Dinosaur dinosaur) {
        this.poseHandler = new PoseHandler(dinosaur);

        ModelProperties properties = dinosaur.getModelProperties();

        for (val entry : properties.getMainModelMap().entrySet()) {
            GrowthStage growth = entry.getKey();
            GrowthStage referneced = growth;
            if(!properties.getModelGrowthStages().contains(growth)) {
                referneced = GrowthStage.ADULT;
            }
            TabulaModel model;
            if(this.modelMap.containsKey(referneced)) {
                model = this.modelMap.get(referneced);
            } else {
                ResourceLocation regname = dinosaur.getRegName();
                String mainModelName = properties.getMainModelMap().get(referneced);
                if(mainModelName == null) {
                    ProjectNublar.getLogger().error("Unable to load model for growth stage {} as main model was not defined", referneced.name());
                    model = null;
                } else {
                    ResourceLocation modelName = new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + referneced.name().toLowerCase(Locale.ROOT) + "/" + mainModelName);
                    try {
                        model = TabulaUtils.getModel(modelName);
                    } catch (Exception e) {
                        ProjectNublar.getLogger().error("Unable to load model: " + modelName.toString(), e);
                        model = null;
                    }
                }
            }
            this.modelMap.put(growth, model);
        }
    }

}
