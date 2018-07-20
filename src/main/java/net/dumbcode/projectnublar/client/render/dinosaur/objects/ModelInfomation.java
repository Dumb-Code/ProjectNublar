package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.client.render.dinosaur.PoseHandler;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelInfomation {
    @SideOnly(Side.CLIENT)
    private Map<String, Map<String, CubeReference>> references; //Map of <Model locaion, <Cube Name, Cube Reference>> //TO-DO: maybe change the first String into an animation ?
    private Map<Animation, List<PoseHandler.ModelData>> animations; //A map of the list of model datas to use in per animation

    public ModelInfomation() {
        this(null);
    }

    @SideOnly(Side.CLIENT)
    public ModelInfomation(Map<String, Map<String, CubeReference>> cuboids, Map<Animation,List<PoseHandler.ModelData>> animations) {
        this(animations);

        if (cuboids == null) {
            cuboids = Maps.newHashMap();
        }

        this.references = cuboids;
    }

    public ModelInfomation(Map<Animation, List<PoseHandler.ModelData>> animations) {
        if (animations == null) {
            animations = new LinkedHashMap<>();
        }

        this.animations = animations;
    }

    public Map<String, Map<String, CubeReference>> getReferences() {
        return references;
    }

    public Map<Animation, List<PoseHandler.ModelData>> getAnimations() {
        return animations;
    }
}