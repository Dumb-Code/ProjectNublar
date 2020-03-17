package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C33SetTabletBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setuppages.SetupPage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class BackgroundTabletScreen extends BaseBackgroundTabletScreen {

    private final int ICONS_PER_COLUMN = 1;
    private final int ICON_SIZE = 128;
    private final int ICON_PADDING = 15;

    private int scroll;

    @Getter
    private SetupPage setupPage = null;
    private int startX;
    private int startY;

    protected BackgroundTabletScreen(EnumHand hand) {
        super(hand);
    }

    @Override
    public void initGui() {
        if(this.setupPage != null) {
            this.setupPage.initPage(this.startX, this.startY);
        }
        super.initGui();
    }

    @Override
    protected void drawTabletScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawTabletScreen(mouseX, mouseY, partialTicks);
        if(this.setupPage != null) {
            drawRect(this.leftStart, this.topStart + 16, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, 0x77000000);
            drawRect(this.startX - 5, this.startY - 5, startX + this.setupPage.getWidth() + 5, this.startY + this.setupPage.getHeight() + 5, 0xFF666666);
            RenderUtils.renderBorder(this.startX - 5, this.startY - 5, this.startX + this.setupPage.getWidth() + 5, this.startY + this.setupPage.getHeight() + 5, 2, 0xFFAAAAAA);
            this.setupPage.render(this.startX, this.startY, mouseX, mouseY);
        } else {
            int entry = 0;
            int fullPageIconsHeight = (ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING);
            for (String s : TabletBackground.REGISTRY.keySet()) {
                int yID = entry % ICONS_PER_COLUMN;
                int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*(ICON_SIZE + ICON_PADDING);

                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/background_icons/" + s + ".png"));
                drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                this.fontRenderer.drawString(s, xPos + ICON_SIZE/2 - this.fontRenderer.getStringWidth(s)/2, yPos + ICON_SIZE + 2, 0xFF888888);
                entry++;
            }
        }
    }

    @Override
    public void updateScreen() {
        if(this.setupPage != null) {
            this.setupPage.updatePage(this.startX, this.startY);
        }
        super.updateScreen();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.setupPage != null) {
            if(mouseX > this.startX - 5 && mouseX < this.startX + this.setupPage.getWidth() + 5 && mouseY > this.startY - 5 && mouseY < this.startY + this.setupPage.getHeight() + 5) {
                this.setupPage.mouseClicked(this.startX, this.startY, mouseX, mouseY, mouseButton);
            } else {
                TabletBackground background = this.setupPage.create();
                this.setBackground(background);
                ProjectNublar.NETWORK.sendToServer(new C33SetTabletBackground(this.hand, background));
                this.setupPage = null;
            }
        } else {
            int entry = 0;
            int fullPageIconsHeight = (ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING);
            for (String s : TabletBackground.REGISTRY.keySet()) {
                int yID = entry % ICONS_PER_COLUMN;
                int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*(ICON_SIZE + ICON_PADDING);

                if(mouseX > xPos && mouseX < xPos + ICON_SIZE && mouseY > yPos && mouseY < yPos + ICON_SIZE) {
                    TabletBackground.Entry<?> e = TabletBackground.REGISTRY.get(s);
                    this.setupPage = e.getSetupPage();
                    if(e.getBackground().getClass() == this.getBackground().getClass()) {
                        this.setupPage.setupFromPage(this.getBackground());
                    }
                    this.startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2;
                    this.startY = this.topStart + this.tabletHeight/2 - this.setupPage.getHeight()/2;
                    this.setupPage.initPage(this.startX, this.startY);
                }

                entry++;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(this.setupPage != null) {
            this.setupPage.mouseClickMove(this.startX, this.startY, mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (this.setupPage != null) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.setupPage.handleMouseInput(this.startX, this.startY, mouseX, mouseY);
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.setupPage != null) {
            this.setupPage.mouseReleased(this.startX, this.startY, mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.setupPage != null) {
            this.setupPage.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
