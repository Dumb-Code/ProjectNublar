package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SSetTabletBackground;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SetupPage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

import java.nio.file.Path;
import java.util.List;

public class BackgroundTabletScreen extends BaseBackgroundTabletScreen {

    private final int ICONS_PER_COLUMN = 1;
    private final int ICON_SIZE = 128;
    private final int ICON_PADDING = 15;

    private int scroll;

    @Getter
    private SetupPage setupPage = null;
    private int startX;
    private int startY;

    protected BackgroundTabletScreen(Hand hand) {
        super(hand);
    }

    @Override
    public void init() {
        if(this.setupPage != null) {
            this.setupPage.initPage(this.startX, this.startY);
        }
        super.init();
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        if(this.setupPage != null) {
            this.setupPage.onFilesDrop(files);
        }
    }

    @Override
    protected void drawTabletScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.drawTabletScreen(stack, mouseX, mouseY, partialTicks);
        if(this.setupPage != null) {
            fill(stack, this.leftStart, this.topStart + 16, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, 0x77000000);
            fill(stack, this.startX - 5, this.startY - 5, startX + this.setupPage.getWidth() + 5, this.startY + this.setupPage.getHeight() + 5, 0xFF666666);
            RenderUtils.renderBorder(stack,this.startX - 5, this.startY - 5, this.startX + this.setupPage.getWidth() + 5, this.startY + this.setupPage.getHeight() + 5, 2, 0xFFAAAAAA);
            this.setupPage.render(stack, mouseX, mouseY, partialTicks);
        } else {
            int entry = 0;
            int fullPageIconsHeight = ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING;
            for (String s : TabletBackground.REGISTRY.keySet()) {
                int yID = entry % ICONS_PER_COLUMN;
                int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*(ICON_SIZE + ICON_PADDING);

                Minecraft.getInstance().textureManager.bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/background_icons/" + s + ".png"));
                blit(stack, xPos, yPos, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                minecraft.font.draw(stack, s, xPos + ICON_SIZE/2F - minecraft.font.width(s)/2F, yPos + ICON_SIZE + 2, 0xFF888888);
                entry++;
            }
        }
    }

    @Override
    public void tick() {
        if(this.setupPage != null) {
            this.setupPage.updatePage();
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            if(this.setupPage != null) {
                if(!(mouseX > this.startX - 5 && mouseX < this.startX + this.setupPage.getWidth() + 5 && mouseY > this.startY - 5 && mouseY < this.startY + this.setupPage.getHeight() + 5)) {
                    TabletBackground background = this.setupPage.create();
                    this.setBackground(background);
                    ProjectNublar.NETWORK.sendToServer(new C2SSetTabletBackground(this.hand, background));
                    this.children.remove(this.setupPage);
                    this.setupPage = null;
                }
                return true;
            } else {
                int entry = 0;
                int fullPageIconsHeight = (ICONS_PER_COLUMN * ICON_SIZE + (ICONS_PER_COLUMN - 1) * ICON_PADDING);
                for (String s : TabletBackground.REGISTRY.keySet()) {
                    int yID = entry % ICONS_PER_COLUMN;
                    int xPos = this.leftStart + 25 + entry/ICONS_PER_COLUMN * (ICON_SIZE + ICON_PADDING);
                    int yPos = this.topStart + this.tabletHeight/2 - fullPageIconsHeight/2 + yID*(ICON_SIZE + ICON_PADDING);

                    if(mouseX > xPos && mouseX < xPos + ICON_SIZE && mouseY > yPos && mouseY < yPos + ICON_SIZE) {
                        TabletBackground.Entry<?> e = TabletBackground.REGISTRY.get(s);
                        this.children.remove(this.setupPage);
                        this.setupPage = this.addWidget(e.getSetupPage());
                        this.startX = this.leftStart + this.tabletWidth/2 - this.setupPage.getWidth()/2;
                        this.startY = this.topStart + this.tabletHeight/2 - this.setupPage.getHeight()/2;
                        this.setupPage.initPage(this.startX, this.startY);
                        if(e.getBackground().getClass() == this.getBackground().getClass()) {
                            this.setupPage.setupFromPage(this.getBackground());
                        }
                    }

                    entry++;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
