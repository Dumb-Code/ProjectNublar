package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public interface GuiConstants {
    /**
     * This color is a white-ish color that is easy on the eyes
     */
    int NICE_WHITE = 0xFFF0F0F0;

    ResourceLocation ROTATION_RING_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/misc/rotation_ring.tbl");

    static boolean mouseOn(GuiButton button, int mouseX, int mouseY) {
        return button.enabled && button.visible && mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;
    }
}
