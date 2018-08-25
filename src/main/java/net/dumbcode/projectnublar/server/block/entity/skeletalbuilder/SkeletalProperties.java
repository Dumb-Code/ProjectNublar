package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.util.EnumFacing;

import java.util.List;

@Data
public class SkeletalProperties {

    private float rotation = 0;
    private final List<String> poleList = Lists.newArrayList();
    private PoleFacing poleFacing = PoleFacing.NONE;
}
