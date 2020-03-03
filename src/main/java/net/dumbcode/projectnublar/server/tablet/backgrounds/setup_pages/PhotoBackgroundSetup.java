package net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C34UploadImage;
import net.dumbcode.projectnublar.server.network.C35RequestAllIcons;
import net.dumbcode.projectnublar.server.network.C40RequestBackgroundIcon;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.input.Mouse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoBackgroundSetup implements SetupPage<PhotoBackground> {

    private DynamicTexture currentTestTexture;
    private BufferedImage currentTestImage;
    private GuiButton globalChangeButton;
    private GuiButton button;

    private final List<BGIconEntry> userEntries = new ArrayList<>();
    private final List<BGIconEntry> globalEntries = new ArrayList<>();
    private boolean updateEntries;

    private BGIconEntry selectedEntry;
    private boolean userTab = true;

    private int scroll;

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
        this.globalChangeButton = new GuiButton(0, x + 130, y + 5, 50, 20, "User");
        this.button = new GuiButton(0, x + 30, y + 110, 50, 20, "Upload");
    }

    public void loadEntries(boolean global, List<TabletBGImageHandler.IconEntry> entries) {
        List<BGIconEntry> list = global ? this.globalEntries : this.userEntries;
        for (BGIconEntry entry : list) {
            if(entry.getTexture() !=  null) {
                entry.getTexture().deleteGlTexture();
            }
        }
        list.clear();
        entries.stream().map(BGIconEntry::new).forEach(list::add);
        this.updateEntries = true;
    }

    @Override
    public void updatePage(int x, int y) {
        if(this.updateEntries) {
            int height = 20;
            int width = (int) (20 * TabletBGImageHandler.SCREEN_RATIO);
            for (List<BGIconEntry> entries : Lists.newArrayList(this.userEntries, this.globalEntries)) {
                for (int i = 0; i < entries.size(); i++) {
                    BGIconEntry entry = entries.get(i);
                    entry.minX = x + 110 + (i%2)*(width + 10);
                    entry.minY = y + 30 + (i/2)*(height + 20);
                    entry.width = width;
                    entry.height = height;
                }
            }
        }
    }

    @Override
    public void render(int x, int y, int mouseX, int mouseY) {
        this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 1F);
        this.globalChangeButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 1F);
        GlStateManager.color(1F, 1F, 1F);
        if(this.currentTestTexture != null) {
            GlStateManager.bindTexture(this.currentTestTexture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 100, 100, 100, 100);
        }

        List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
        for (BGIconEntry entry : list) {
            if(entry.minY + entry.height + this.scroll - 5 < y + this.getHeight() && !entry.requested) {
                entry.requested = true;
                ProjectNublar.NETWORK.sendToServer(new C40RequestBackgroundIcon(entry.iconEntry.getUploaderUUID(), entry.iconEntry.getImageHash(), !this.userTab));
            }
            if(entry.texture != null) {
                GlStateManager.bindTexture(entry.texture.getGlTextureId());
                Gui.drawModalRectWithCustomSizedTexture(entry.minX, entry.minY + this.scroll, 0, 0, entry.width, entry.height, entry.width, entry.height);
                if(entry == this.selectedEntry) {
                    Gui.drawRect(entry.minX - 2, entry.minY + this.scroll - 2, entry.minX + entry.width + 2, entry.minY + entry.height + 2 + this.scroll, 0x8800BAFD);
                    GlStateManager.color(1F, 1F, 1F);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        if(this.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY) && this.currentTestImage != null) {
            int maxWidth = 250;
            int maxHeight = (int) (maxWidth / TabletBGImageHandler.SCREEN_RATIO);

            int width = this.currentTestImage.getWidth();
            int height = this.currentTestImage.getHeight();

            BufferedImage image = this.currentTestImage;
            if(width >= maxWidth || height >= maxHeight) {
                float ratio = (float) width / height;
                int newWidth = (int) Math.min(maxWidth, width * ratio);
                int newHeight = (int) (newWidth / ratio);
                image = TextureUtils.resize(image, newWidth, newHeight);
            }
            ProjectNublar.NETWORK.sendToServer(new C34UploadImage(!this.userTab, image));
        }

        if(this.globalChangeButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            this.globalChangeButton.displayString = this.userTab ? "Global" : "User";
            this.userTab = !this.userTab;
            ProjectNublar.NETWORK.sendToServer(new C35RequestAllIcons(!this.userTab));
        }

        List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
        for (BGIconEntry entry : list) {
            if(mouseX > entry.minX && mouseX < entry.minX + entry.width && mouseY > entry.minY + this.scroll && mouseY < entry.minY + entry.height + this.scroll) {
                this.selectedEntry = entry;
                break;
            }
        }
    }

    @Override
    public void handleMouseInput(int x, int y, int mouseX, int mouseY) {
        this.scroll += Math.signum(Mouse.getDWheel()) * 10;
    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.button.mouseReleased(mouseX, mouseY);
        this.globalChangeButton.mouseReleased(mouseX, mouseY);
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
        PhotoBackground background = new PhotoBackground();
        if(this.selectedEntry != null) {
            background.setPhoto(this.selectedEntry.iconEntry.getUploaderUUID(), this.selectedEntry.iconEntry.getImageHash());
        }
        return background;
    }

    public void loadIcon(String uploaderUUID, String imageHash, boolean global, BufferedImage image) {
        List<BGIconEntry> list = global ? this.globalEntries : this.userEntries;
        for (BGIconEntry entry : list) {
            if(entry.iconEntry.getUploaderUUID().equals(uploaderUUID) && entry.iconEntry.getImageHash().equals(imageHash)) {
                if(entry.texture != null) {
                    entry.texture.deleteGlTexture();
                }
                entry.texture = new DynamicTexture(image);
                break;
            }
        }

    }

    @Getter
    private class BGIconEntry {

        private int minX;
        private int minY;
        private int width;
        private int height;

        private final TabletBGImageHandler.IconEntry iconEntry;
        private DynamicTexture texture;

        private boolean requested;

        private BGIconEntry(TabletBGImageHandler.IconEntry iconEntry) {
            this.iconEntry = iconEntry;
        }
    }
}
