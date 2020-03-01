package net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages;

import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C34UploadImage;
import net.dumbcode.projectnublar.server.network.C35RequestAllIcons;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoBackgroundSetup implements SetupPage<PhotoBackground> {

    private DynamicTexture currentTestTexture;
    private BufferedImage currentTestImage;
    private GuiButton button;

    private final List<BGIconEntry> userEntries = new ArrayList<>();
    private final List<BGIconEntry> globalEntries = new ArrayList<>();

    private boolean userTab = true;

    public PhotoBackgroundSetup() {
        ProjectNublar.NETWORK.sendToServer(new C35RequestAllIcons(false));
    }

    @Override
    public int getWidth() {
        return 190;
    }

    @Override
    public int getHeight() {
        return 130;
    }

    @Override
    public void initPage(int x, int y) {
        this.button = new GuiButton(0, x + 30, y + 110, 50, 20, "Upload");
    }

    public void loadEntries(boolean global, List<TabletBGImageHandler.IconEntry> entries) {
        List<BGIconEntry> list = global ? this.globalEntries : this.userEntries;
        for (BGIconEntry entry : list) {
            entry.getTexture().deleteGlTexture();
        }
        list.clear();
        entries.stream().map(BGIconEntry::new).forEach(list::add);
    }

    @Override
    public void render(int x, int y, int mouseX, int mouseY) {
        this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 1F);
        GlStateManager.color(1F, 1F, 1F);
        if(this.currentTestTexture != null) {
            GlStateManager.bindTexture(this.currentTestTexture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 100, 100, 100, 100);
        }

        int height = 20;
        int width = (int) (20 * TabletBGImageHandler.SCREEN_RATIO);

        List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
        for (int i = 0; i < list.size(); i++) {
            BGIconEntry entry = list.get(i);
            int xPos = x + 110 + (i%2)*(width + 10);
            int yPos = y + 10 + (i/2)*(height + 20);

            GlStateManager.bindTexture(entry.texture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, width, height, width, height);
        }
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        if(this.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY) && this.currentTestImage != null) {
            ProjectNublar.NETWORK.sendToServer(new C34UploadImage(!this.userTab, this.currentTestImage));
        }
    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.button.mouseReleased(mouseX, mouseY);

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if(GuiScreen.isKeyComboCtrlV(keyCode)) {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null) {
                try {
                    BufferedImage image = null;
                    if(transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        Image copied = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
                        if(copied instanceof BufferedImage) {
                            image = (BufferedImage) copied;
                        } else {
                            image = new BufferedImage(copied.getWidth(null), copied.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                            Graphics2D g = image.createGraphics();
                            g.drawImage(copied, 0, 0, null);
                            g.dispose();
                        }
                    } else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        image = ImageIO.read(fileList.get(0));
                    }

                    if(image != null) {
                        image = TextureUtils.resize(image, image.getWidth(), image.getHeight()); //If the user uploads a gif, it uploads ALL of the frames. This is to make sure it's just one frame
                        if(this.currentTestTexture != null) {
                            this.currentTestTexture.deleteGlTexture();
                        }
                        this.currentTestTexture = new DynamicTexture(image);
                        this.currentTestImage = image;
                    }
                } catch (IOException | UnsupportedFlavorException e) {
                    DumbLibrary.getLogger().error("Unable to paste clipboard", e);
                }
            }
        }
    }

    @Override
    public PhotoBackground create() {
        return new PhotoBackground();
    }

    @Getter
    public class BGIconEntry {

        private final TabletBGImageHandler.IconEntry iconEntry;
        private DynamicTexture texture;

        public BGIconEntry(TabletBGImageHandler.IconEntry iconEntry) {
            this.iconEntry = iconEntry;
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(iconEntry.getImageData()));
                this.texture = new DynamicTexture(image);
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to load icon texture", e);
            }
        }
    }
}
