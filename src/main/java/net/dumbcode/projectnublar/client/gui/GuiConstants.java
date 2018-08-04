package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public interface GuiConstants {
    /**
     * This color is a white-ish color that is easy on the eyes
     */
    int NICE_WHITE = 0xFFF0F0F0;

    ResourceLocation ROTATION_RING_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/misc/rotation_ring.tbl");

    TextComponentTranslation LEFT_CLICK_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.left_click");
    TextComponentTranslation CONTROLS_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls");
    TextComponentTranslation MIDDLE_CLICK_DRAG_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.middle_click_drag");
    TextComponentTranslation MOVEMENT_KEYS_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.movement_keys");
    TextComponentTranslation ARROW_KEYS_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.arrow_keys");
    TextComponentTranslation TRACKPAD_ZOOM_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.trackpad_zoom");
    TextComponentTranslation MOUSE_WHEEL_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".gui.controls.mouse_wheel");

    static boolean mouseOn(GuiButton button, int mouseX, int mouseY) {
        return button.enabled && button.visible && mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;
    }
}
