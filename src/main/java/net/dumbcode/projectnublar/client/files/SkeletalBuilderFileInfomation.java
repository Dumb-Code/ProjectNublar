package net.dumbcode.projectnublar.client.files;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;

@Value
public class SkeletalBuilderFileInfomation {
    ResourceLocation dinosaurLocation;
    Map<String, Vector3f> poseData;
}
