package net.dumbcode.projectnublar.client.render.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lombok.Cleanup;
import lombok.Value;
import lombok.val;
import net.dumbcode.projectnublar.client.render.dinosaur.objects.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.util.*;

public class PoseHandler {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(PoseObject.class, PoseObject.Deserializer.INSTANCE)
            .registerTypeAdapter(DinosaurAnimationInfomation.class, DinosaurAnimationInfomation.Deserializer.INSTANCE)
            .create();

    private final Map<GrowthStage, ModelInfomation> modelInfomationMap = new EnumMap<>(GrowthStage.class);

    public PoseHandler(Dinosaur dinosaur) {
        this(dinosaur.getRegName(), dinosaur.getModelGrowthStages());
    }

    public PoseHandler(ResourceLocation regname, List<GrowthStage> growthStages) {
        String baseLoc = "models/entities/" + regname.getResourcePath() + "/";
        for (GrowthStage growth : GrowthStage.values()) {
            GrowthStage reference = growth;
            if(!growthStages.contains(growth)) {
                reference = GrowthStage.ADULT;
            }

            ModelInfomation info;
            if(this.modelInfomationMap.containsKey(reference)) {
                info = this.modelInfomationMap.get(reference);
            } else {
                String growthName = reference.name().toLowerCase(Locale.ROOT);
                String growthDirectory = baseLoc + growthName + "/";
                String jsonFile = growthDirectory + regname.getResourcePath() + "_" + growthName + ".json";
                InputStream jsonStream;
                DinosaurAnimationInfomation rawData;
                try {
                    jsonStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(regname.getResourceDomain(), jsonFile)).getInputStream();
                    @Cleanup Reader reader = new InputStreamReader(jsonStream);
                    rawData = GSON.fromJson(reader, DinosaurAnimationInfomation.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not load input stream for " + regname, e);
                }
                if (rawData.getPoses().get(DinosaurAnimations.IDLE) == null || rawData.getPoses().get(DinosaurAnimations.IDLE).isEmpty()) {
                    throw new IllegalArgumentException("Animation files must define at least one pose for the IDLE animation");
                }
                List<ModelLocation> posedModelResources = Lists.newArrayList();
                for (List<PoseObject> poses : rawData.getPoses().values()) {
                    for (PoseObject pose : poses) {
                        String fileLoc = growthDirectory + pose.getPoseLocation();
                        ModelLocation loc = new ModelLocation(pose.getPoseLocation(), fileLoc);
                        if(!posedModelResources.contains(loc)) {
                            posedModelResources.add(loc);
                        }
                    }
                }

                Map<Animation, List<ModelData>> animationMap = Maps.newHashMap();
                for (val entry : rawData.getPoses().entrySet()) {
                    Animation animation = entry.getKey().get();
                    List<ModelData> data = animationMap.computeIfAbsent(animation, a -> Lists.newArrayList());
                    for (PoseObject pose : entry.getValue()) {
                        data.add(new ModelData(pose.getPoseLocation(), pose.getTicksTime()));
                    }
                }
                if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                    info = loadClientInfomation(new ResourceLocation(regname.getResourceDomain(), posedModelResources.get(0).getFullLocation()), posedModelResources, animationMap);
                } else {
                    info = new ModelInfomation(animationMap);
                }
            }
            this.modelInfomationMap.put(growth, info);
        }
    }

    @SideOnly(Side.CLIENT)
    private ModelInfomation loadClientInfomation(ResourceLocation mainModelLocation, List<ModelLocation> posedModelResources, Map<Animation, List<ModelData>> animations) {
        TabulaModel mainModel = TabulaUtils.getModel(mainModelLocation);
        Map<String, Map<String, CubeReference>> map = Maps.newHashMap(); //Map of <Model locaion, <Cube Name, Cube Reference>>
        for (ModelLocation modelResource : posedModelResources) {
            val innerMap = map.computeIfAbsent(modelResource.getFileName(), s -> Maps.newHashMap());
            ResourceLocation location = new ResourceLocation(mainModelLocation.getResourceDomain(), modelResource.getFullLocation());
            if(location.equals(mainModelLocation)) {
                for (val cube : mainModel.getCubes().entrySet()) {
                    innerMap.put(cube.getKey(), CubeReference.fromCube(cube.getValue()));
                }
            } else {
                if(modelResource.getFileName().endsWith(".tbl") || true) { //The old way. Currently only the working way. Need to check the integrity of the python script
                    TabulaModel model = TabulaUtils.getModel(location);
                    for (String cubeName : mainModel.getCubes().keySet()) {
                        AdvancedModelRenderer cube = model.getCube(cubeName);
                        if(cube == null) {
                            cube = mainModel.getCube(cubeName);
                        }
                        innerMap.put(cubeName, CubeReference.fromCube(cube));
                    }
                } else {
                    try {
                        JsonParser parser = new JsonParser();
                        if(!location.getResourcePath().endsWith(".json")) {
                            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
                        }
                        @Cleanup InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
                        @Cleanup InputStreamReader reader = new InputStreamReader(stream);
                        JsonObject json = parser.parse(reader).getAsJsonObject();
                        int version = JsonUtils.getInt(json, "version");
                        List<String> cubeNames = Lists.newArrayList(mainModel.getCubes().keySet());
                        for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "overrides")) {
                            JsonObject obj = jsonElement.getAsJsonObject();
                            String cubeName = JsonUtils.getString(obj, "cube_name");
                            AdvancedModelRenderer mainCube = mainModel.getCube(cubeName);

                            if(!cubeNames.contains(cubeName) || mainCube == null) {
                                continue;
                            } else {
                                cubeNames.remove(cubeName);
                            }
                            switch (version) {
                                case 0:
                                    innerMap.put(cubeName, new CubeReference(
                                            JsonUtils.hasField(obj, "rotation_x") ? JsonUtils.getFloat(obj, "rotation_x") : mainCube.defaultRotationX,
                                            JsonUtils.hasField(obj, "rotation_y") ? JsonUtils.getFloat(obj, "rotation_y") : mainCube.defaultRotationY,
                                            JsonUtils.hasField(obj, "rotation_z") ? JsonUtils.getFloat(obj, "rotation_z") : mainCube.defaultRotationZ,
                                            JsonUtils.hasField(obj, "position_x") ? JsonUtils.getFloat(obj, "position_x") : mainCube.defaultPositionX,
                                            JsonUtils.hasField(obj, "position_y") ? JsonUtils.getFloat(obj, "position_y") : mainCube.defaultPositionY,
                                            JsonUtils.hasField(obj, "position_z") ? JsonUtils.getFloat(obj, "position_z") : mainCube.defaultPositionZ
                                    ));
                                    break;

                                default:
                                    throw new IllegalArgumentException("Dont know how to handle version " + version);
                            }
                        }
                        for (String cubeName : cubeNames) {
                            innerMap.put(cubeName, CubeReference.fromCube(mainModel.getCube(cubeName)));

                        }
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return new ModelInfomation(map, animations);
    }

    public AnimationPass createPass(DinosaurEntity entity, TabulaModel model, GrowthStage growthStage, boolean useInertialTweens) {
        ModelInfomation modelInfo = this.modelInfomationMap.get(growthStage);
        if (!entity.getDinosaur().getModelGrowthStages().contains(growthStage)) {
            modelInfo = this.modelInfomationMap.get(GrowthStage.ADULT);
        }
        AnimationPass pass = new AnimationPass(modelInfo.getAnimations(), modelInfo.getReferences(), useInertialTweens);
        pass.init(model, entity);
        return pass;
    }

    private Map<Animation, List<ModelData>> getAnimations(GrowthStage growthStage) {
        return this.modelInfomationMap.get(growthStage).getAnimations();
    }

    public float getAnimationLength(Animation animation, GrowthStage growthStage) {
        Map<Animation, List<ModelData>> animations = this.getAnimations(growthStage);
        float duration = 0;
        if (animation != null) {
            List<ModelData> poses = animations.getOrDefault(animation, Lists.newArrayList());
            for (ModelData pose : poses) {
                duration += pose.getTime();
            }
        }
        return duration;
    }

    @Value
    public class ModelData {
        String modelName;
        float time;
    }

    @Value
    private class ModelLocation {
        String fileName;
        String fullLocation;

        @Override
        public int hashCode() {
            return this.fullLocation.hashCode();
        }
    }
}
