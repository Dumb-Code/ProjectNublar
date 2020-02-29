package net.dumbcode.projectnublar.client.gui.tablet;

import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.network.C33SetTabletBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.SetupPage;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class BackgroundTabletScreen extends BaseTabletScreen {

    private final int ICONS_PER_COLUMN = 3;
    private final int ICON_SIZE = 32;
    private final int ICON_PADDING = 15;

    private int scroll;

    private SetupPage setupPage = null;

    protected BackgroundTabletScreen(EnumHand hand) {
        super(hand);
    }

    @Override
    protected void drawTabletScreen(int mouseX, int mouseY, float partialTicks) {
        if(this.setupPage != null) {
            int startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2 - 5;
            int startY = this.topStart+ this.tabletHeight/2 - this.setupPage.getHeight()/2 - 5;
            drawRect(this.leftStart, this.topStart + 16, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, 0x77000000);
            drawRect(startX, startY, startX + this.setupPage.getWidth() + 10, startY + this.setupPage.getHeight() + 10, 0xFF666666);
            RenderUtils.renderBorder(startX, startY, startX + this.setupPage.getWidth() + 10, startY + this.setupPage.getHeight() + 10, 2, 0xFFAAAAAA);
            this.setupPage.render(startX + 5, startY + 5, mouseX - 5, mouseY - 5);
        } else {
            int entry = 0;
            int fullPageIconsHeight = (ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING);
            for (String s : TabletBackground.REGISTRY.keySet()) {
                int yID = entry % ICONS_PER_COLUMN;
                int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*ICON_SIZE + Math.max(yID-1,0)* ICON_PADDING;

                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/background_icons/" + s + ".png"));
                drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                this.fontRenderer.drawString(s, xPos + ICON_SIZE/2 - this.fontRenderer.getStringWidth(s)/2, yPos + ICON_SIZE + 2, 0xFF888888);
                entry++;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.setupPage != null) {
            int startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2 - 5;
            int startY = this.topStart+ this.tabletHeight/2 - this.setupPage.getHeight()/2 - 5;
            if(mouseX > startX && mouseX < startX +this.setupPage.getWidth() + 10 && mouseY > startY && mouseY < startY + this.setupPage.getHeight() + 10) {
                this.setupPage.mouseClicked(startX + 5, startY + 5, mouseX - 5, mouseY - 5, mouseButton);
            } else {
                ProjectNublar.NETWORK.sendToServer(new C33SetTabletBackground(this.hand, this.setupPage.create()));
                this.setupPage = null;
            }
        } else {
            int entry = 0;
            int fullPageIconsHeight = (ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING);
            for (String s : TabletBackground.REGISTRY.keySet()) {
                int yID = entry % ICONS_PER_COLUMN;
                int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*ICON_SIZE + Math.max(yID-1,0)* ICON_PADDING;

                if(mouseX > xPos && mouseX < xPos + ICON_SIZE && mouseY > yPos && mouseY < yPos + ICON_SIZE) {
                    this.setupPage = TabletBackground.REGISTRY.get(s).getSetupPage().get();
                }

                entry++;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(this.setupPage != null) {
            int startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2 - 5;
            int startY = this.topStart+ this.tabletHeight/2 - this.setupPage.getHeight()/2 - 5;
            this.setupPage.mouseClickMove(startX + 5, startY + 5, mouseX - 5, mouseY - 5, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.setupPage != null) {
            int startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2 - 5;
            int startY = this.topStart+ this.tabletHeight/2 - this.setupPage.getHeight()/2 - 5;
            this.setupPage.mouseReleased(startX + 5, startY + 5, mouseX - 5, mouseY - 5, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
}
