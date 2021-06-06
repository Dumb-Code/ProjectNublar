package net.dumbcode.projectnublar.server.data;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ProjectNublarBlockTags {

    public static final ITag.INamedTag<Block> FOSSIL_CLAY_REPLACEMENT = tag("fossil_clay_replacement");
    public static final ITag.INamedTag<Block> FOSSIL_SANDSTONE_REPLACEMENT = tag("fossil_sandstone_replacement");

    public static ITag.INamedTag<Block> tag(String id) {
        return BlockTags.createOptional(new ResourceLocation(ProjectNublar.MODID, id));
    }
}
