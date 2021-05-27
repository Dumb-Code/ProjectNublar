package net.dumbcode.projectnublar.client.files;

import lombok.Value;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Map;

@Value
public class SkeletalBuilderFileInfomation {
    ResourceLocation dinosaurLocation;
    Map<String, Vector3f> poseData;
}
