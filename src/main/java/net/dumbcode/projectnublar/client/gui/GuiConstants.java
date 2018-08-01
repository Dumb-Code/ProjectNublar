package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;

public interface GuiConstants {
    /**
     * This color is a white-ish color that is easy on the eyes
     */
    int NICE_WHITE = 0xFFF0F0F0;

    ResourceLocation ROTATION_RING_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/misc/rotation_ring.tbl");

    double DEGREES_TO_RADIANS = Math.PI/180.0;
    float DEGREES_TO_RADIANS_f = (float)DEGREES_TO_RADIANS;
}
