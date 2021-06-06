package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SPhotoBackgroundRequestAllIcons;
import net.dumbcode.projectnublar.server.network.C2SRequestPhotoBackgroundIcon;
import net.dumbcode.projectnublar.server.network.CS2UploadPhotoBackgroundImage;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.text.StringTextComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoBackgroundSetup extends SetupPage<PhotoBackground> {

    private DynamicTexture currentTestTexture;
    private NativeImage currentTestImage;
    private Button globalChangeButton;
    private Button button;

    private final List<BGIconEntry> userEntries = new ArrayList<>();
    private final List<BGIconEntry> globalEntries = new ArrayList<>();
    private boolean updateEntries;

    private BGIconEntry selectedEntry;
    private boolean userTab = true;

    private int scroll;

    public PhotoBackgroundSetup() {
        super(190, 130);
        ProjectNublar.NETWORK.sendToServer(new C2SPhotoBackgroundRequestAllIcons(false));
    }


    @Override
    public void initPage(int x, int y) {
        this.globalChangeButton = this.add(new Button(x + 130, y + 5, 50, 20, new StringTextComponent("User"), b -> {
            this.globalChangeButton.setMessage(new StringTextComponent(this.userTab ? "Global" : "User"));
            this.userTab = !this.userTab;
            ProjectNublar.NETWORK.sendToServer(new C2SPhotoBackgroundRequestAllIcons(!this.userTab));
        }));
        this.button = this.add(new Button(x + 30, y + 110, 50, 20, new StringTextComponent("Upload"), b -> {
            int maxWidth = 250;
            int maxHeight = (int) (maxWidth / TabletBGImageHandler.SCREEN_RATIO);

            int width = this.currentTestImage.getWidth();
            int height = this.currentTestImage.getHeight();

            NativeImage image = this.currentTestImage;
            if(width >= maxWidth || height >= maxHeight) {
                float ratio = (float) width / height;
                int newWidth = (int) Math.min(maxWidth, width * ratio);
                int newHeight = (int) (newWidth / ratio);
                image = TextureUtils.resize(image, newWidth, newHeight);
            }
            try {
                ProjectNublar.NETWORK.sendToServer(new CS2UploadPhotoBackgroundImage(!this.userTab, image.asByteArray()));
            } catch (IOException e) {
                ProjectNublar.getLogger().error("Unable to write image", e);
            }
        }));
        super.initPage(x, y);
    }

    public void loadEntries(boolean global, List<TabletBGImageHandler.IconEntry> entries) {
        List<BGIconEntry> list = global ? this.globalEntries : this.userEntries;
        for (BGIconEntry entry : list) {
            if(entry.getTexture() !=  null) {
                entry.getTexture().close();
            }
        }
        list.clear();
        entries.stream().map(BGIconEntry::new).forEach(list::add);
        this.updateEntries = true;
    }

    @Override
    public void updatePage() {
        if(this.updateEntries) {
            int height = 20;
            int width = (int) (20 * TabletBGImageHandler.SCREEN_RATIO);
            for (List<BGIconEntry> entries : Lists.newArrayList(this.userEntries, this.globalEntries)) {
                for (int i = 0; i < entries.size(); i++) {
                    BGIconEntry entry = entries.get(i);
                    entry.minX = this.x + 110 + (i%2)*(width + 10);
                    entry.minY = this.y + 30 + (i/2)*(height + 20);
                    entry.width = width;
                    entry.height = height;
                }
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.button.render(stack, mouseX, mouseY, partialTicks);
        this.globalChangeButton.render(stack, mouseX, mouseY, partialTicks);

        if(this.currentTestTexture != null) {
            this.currentTestTexture.bind();
            AbstractGui.blit(stack, x, y, 0, 0, 100, 100, 100, 100);
        }

        List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
        for (BGIconEntry entry : list) {
            if(entry.minY + entry.height + this.scroll - 5 < y + this.getHeight() && !entry.requested) {
                entry.requested = true;
                ProjectNublar.NETWORK.sendToServer(new C2SRequestPhotoBackgroundIcon(!this.userTab, entry.iconEntry.getUploaderUUID(), entry.iconEntry.getImageHash()));
            }
            if(entry.texture != null) {
                entry.texture.bind();
                AbstractGui.blit(stack, entry.minX, entry.minY + this.scroll, 0, 0, entry.width, entry.height, entry.width, entry.height);
                if(entry == this.selectedEntry) {
                    AbstractGui.fill(stack, entry.minX - 2, entry.minY + this.scroll - 2, entry.minX + entry.width + 2, entry.minY + entry.height + 2 + this.scroll, 0x8800BAFD);
                }
            }
        }
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
            for (BGIconEntry entry : list) {
                if (mouseX > entry.minX && mouseX < entry.minX + entry.width && mouseY > entry.minY + this.scroll && mouseY < entry.minY + entry.height + this.scroll) {
                    this.selectedEntry = entry;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.scroll += scrollAmount * 10;
        return true;
    }


//    Opublic void keyTyped(char typedChar, int keyCode) {
//        if(GuiScreen.isKeyComboCtrlV(keyCode)) {
//            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
//            if (transferable != null) {
//                try {
//                    BufferedImage image = null;
//                    if(transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
//                        Image copied = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
//                        if(copied instanceof BufferedImage) {
//                            image = (BufferedImage) copied;
//                        } else {
//                            image = new BufferedImage(copied.getWidth(null), copied.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//
//                            Graphics2D g = image.createGraphics();
//                            g.drawImage(copied, 0, 0, null);
//                            g.dispose();
//                        }
//                    } else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                        List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
//                        image = ImageIO.read(fileList.get(0));
//                    }
//
//                    if(image != null) {
//                        image = TextureUtils.resize(image, image.getWidth(), image.getHeight()); //If the user uploads a gif, it uploads ALL of the frames. This is to make sure it's just one frame
//                        if(this.currentTestTexture != null) {
//                            this.currentTestTexture.deleteGlTexture();
//                        }
//                        this.currentTestTexture = new DynamicTexture(image);
//                        this.currentTestImage = image;
//                    }
//                } catch (IOException | UnsupportedFlavorException e) {
//                    DumbLibrary.getLogger().error("Unable to paste clipboard", e);
//                }
//            }
//        }
//    }

    @Override
    public PhotoBackground create() {
        PhotoBackground background = new PhotoBackground();
        if(this.selectedEntry != null) {
            background.setPhoto(this.selectedEntry.iconEntry.getUploaderUUID(), this.selectedEntry.iconEntry.getImageHash());
        }
        return background;
    }

    public void loadIcon(String uploaderUUID, String imageHash, boolean global, NativeImage image) {
        List<BGIconEntry> list = global ? this.globalEntries : this.userEntries;
        for (BGIconEntry entry : list) {
            if(entry.iconEntry.getUploaderUUID().equals(uploaderUUID) && entry.iconEntry.getImageHash().equals(imageHash)) {
                if(entry.texture != null) {
                    entry.texture.releaseId();
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
