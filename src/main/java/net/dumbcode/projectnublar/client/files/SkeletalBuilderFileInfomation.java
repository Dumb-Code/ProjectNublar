package net.dumbcode.projectnublar.client.files;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.Map;

@Value
public class SkeletalBuilderFileInfomation {
    ResourceLocation dinosaurLocation;
    Map<String, Vector3f> poseData;
}
