package net.dumbcode.projectnublar.server.world.structures;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.placement.PushdownPlacement;
import net.minecraft.util.ResourceLocation;

public class Structures {

    public static final NBTTemplate GREENTENT_LARGE = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/greentent_large"), new PushdownPlacement(1));
    public static final NBTTemplate GREENTENT_SMALL = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/greentent_small"), new PushdownPlacement(1));

    public static final NBTTemplate CRATE_SMALL = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_small"), new PushdownPlacement(0));
    public static final NBTTemplate CRATE_MEDIUM = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_medium"), new PushdownPlacement(0));
    public static final NBTTemplate CRATE_LARGE = NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_large"), new PushdownPlacement(0));


}
