package net.dumbcode.projectnublar.client.render;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL14;

import java.util.List;

@UtilityClass
public class MoreTabulaUtils {
    public static List<ModelRenderer> getAllChildren(ModelRenderer box, List<String> ignoreList) {
        if(box.childModels == null) {
            return Lists.newArrayList();
        }
        List<ModelRenderer> list = Lists.newArrayList();
        list.add(box);
        for (ModelRenderer childModel : box.childModels) {
            if(!ignoreList.contains(childModel.boxName)) {
                list.add(childModel);
                list.addAll(getAllChildren(childModel, ignoreList));
            }
        }
        return list;
    }

    public static void renderModelWithoutChangingPose(TabulaModel model, float scale) {
        GL14.glBlendColor(1F, 1F, 1F, 1f);

        GlStateManager.pushMatrix();
        double[] modelScale = ReflectionHelper.getPrivateValue(TabulaModel.class, model, "scale");
        List<AdvancedModelRenderer> rootBoxes = ReflectionHelper.getPrivateValue(TabulaModel.class, model, "rootBoxes");
        GlStateManager.scale(modelScale[0], modelScale[1], modelScale[2]);
        for (AdvancedModelRenderer box : rootBoxes) {
            box.render(scale);
        }
        GlStateManager.popMatrix();
    }
}
