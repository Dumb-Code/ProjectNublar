package net.dumbcode.projectnublar.client.render.animator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.dumbcode.projectnublar.client.render.MoreTabulaUtils;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.model.ModelRenderer;

import java.util.List;
import java.util.Map;

public class DinosaurAnimator extends EntityAnimator<DinosaurEntity, ModelStage> {

    private boolean rescalingEnabled = true;

    public DinosaurAnimator(PoseHandler<DinosaurEntity, ModelStage> poseHandler) {
        super(poseHandler);
    }

    @Override
    protected void performAnimations(TabulaModel parModel, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
        if(!rescalingEnabled) {
            return;
        }
        Dinosaur dinosaur = entity.getDinosaur();
        Map<String, List<ModelRenderer>> modelChildMap = Maps.newHashMap();
        List<String> modelList = Lists.newArrayList();
        for (String s : dinosaur.getSkeletalInformation().getIndividualBones()) {
            modelList.addAll(dinosaur.getSkeletalInformation().getBoneToModelMap().get(s));
        }
        int id = entity.modelIndex % (modelList.size() + 1);
        List<ModelRenderer> nonHiddenCubes = Lists.newArrayList();
        if(id != 0) {
            String currentState = modelList.get(id - 1);
            List<String> activeStates = Lists.newArrayList();
            for (int i = 0; i < modelList.size(); i++) {
                String model = modelList.get(i);
                modelChildMap.put(model, MoreTabulaUtils.getAllChildren(parModel.getCube(model), modelList));
                if(i <= modelList.indexOf(currentState)) {
                    activeStates.add(model);
                }
            }
            for (String activeState : activeStates) {
                nonHiddenCubes.addAll(modelChildMap.get(activeState));
            }
            for (ModelRenderer modelRenderer : parModel.boxList) {
                AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
                if(nonHiddenCubes.contains(modelRenderer)) {
                    box.scaleX = 1;
                    box.scaleY = 1;
                    box.scaleZ = 1;
                } else {
                    box.scaleX = 0;
                    box.scaleY = 0;
                    box.scaleZ = 0;
                }
            }
        } else {
            for (ModelRenderer modelRenderer : parModel.boxList) {
                AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
                box.scaleX = 1;
                box.scaleY = 1;
                box.scaleZ = 1;
            }
        }
        super.performAnimations(parModel, entity, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
    }

    /**
     * Sets an internal flag that tells the animator not to rescale the model parts to their normal size when rendering non-hidden parts. Used in {@link net.dumbcode.projectnublar.client.gui.GuiSkeletalBuilder GuiSkeletalBuilder}
     * @param rescalingEnabled Should non hidden parts be rescaled?
     */
    public void setRescalingEnabled(boolean rescalingEnabled) {
        this.rescalingEnabled = rescalingEnabled;
    }
}
