package net.dumbcode.projectnublar.server.world.structures;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.placement.ExtendPlacement;
import net.dumbcode.projectnublar.server.world.structures.structures.placement.PushdownPlacement;
import net.minecraft.util.ResourceLocation;

public class Structures {

    public static final NBTTemplate TENT_LARGE_1 = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/tent_large_1"), new PushdownPlacement(1));
    public static final NBTTemplate TENT_SMALL_1 = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/tent_small_1"), new ExtendPlacement(1));

    public static final NBTTemplate TENT_LARGE_2 = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/tent_large_2"), new ExtendPlacement(1));
    public static final NBTTemplate TENT_SMALL_2 = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/tent_small_2"), new ExtendPlacement(1));

    public static final NBTTemplate PREBUILT = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/prebuilt_digsite_1"), new PushdownPlacement(5));

    public static final NBTTemplate CRATE_SMALL = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_small"), new PushdownPlacement(0));
    public static final NBTTemplate CRATE_MEDIUM = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_medium"), new PushdownPlacement(0));
    public static final NBTTemplate CRATE_LARGE = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_large"), new PushdownPlacement(0));


}
