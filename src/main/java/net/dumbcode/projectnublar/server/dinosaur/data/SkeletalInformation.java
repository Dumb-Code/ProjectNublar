package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
public class SkeletalInformation {
    private List<String> individualBones = Lists.newArrayList();
    private List<String> boneListed = Lists.newArrayList();
    private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

    public void initilizeMap(String... boneModels) {
        if(boneModels.length % 2 != 0) {
            throw new RuntimeException("Dont know how to handle list of length " + boneModels.length);
        }
        for (int i = 0; i < boneModels.length; i+=2) {
            String bone = boneModels[i];
            if(!this.individualBones.contains(bone)) {
                this.individualBones.add(bone);
            }
            this.boneListed.add(bone);
            this.boneToModelMap.computeIfAbsent(bone, s -> Lists.newArrayList()).add(boneModels[i + 1]);
        }
    }
}
