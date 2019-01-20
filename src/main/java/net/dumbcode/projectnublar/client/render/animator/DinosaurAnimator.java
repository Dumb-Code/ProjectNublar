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
