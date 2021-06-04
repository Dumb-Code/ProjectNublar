package net.dumbcode.projectnublar.client.render;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;

import java.util.List;

@UtilityClass
public class MoreTabulaUtils {
    public static List<DCMModelRenderer> getAllChildren(DCMModelRenderer box, List<String> ignoreList) {
        if(box.getChildCubes() == null) {
            return Lists.newArrayList();
        }
        List<DCMModelRenderer> list = Lists.newArrayList(box);
        for (DCMModelRenderer childModel : box.getChildCubes()) {
            if(!ignoreList.contains(childModel.getName())) {
                list.add(childModel);
                list.addAll(getAllChildren(childModel, ignoreList));
            }
        }
        return list;
    }
}
