package net.dumbcode.projectnublar.client.render;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.minecraft.client.model.ModelRenderer;

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
}
