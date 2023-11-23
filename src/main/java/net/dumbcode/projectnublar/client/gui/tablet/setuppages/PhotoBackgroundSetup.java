package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.projectnublar.mixin.ButtonAccessor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SPhotoBackgroundRequestAllIcons;
import net.dumbcode.projectnublar.server.network.C2SRequestPhotoBackgroundIcon;
import net.dumbcode.projectnublar.server.network.CS2UploadPhotoBackgroundImage;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PhotoBackgroundSetup extends SetupPage<PhotoBackground> {

    private DynamicTexture currentTestTexture;
    private NativeImage currentTestImage;
    private Button globalChangeButton;
    private Button openFileButton;
    private Button uploadButton;

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
        this.globalChangeButton = this.add(ButtonAccessor.construct(x + 130, y + 5, 50, 20, Component.translatable(ProjectNublar.MODID + ".gui.photo.mode.user"), b -> {
            this.globalChangeButton.setMessage(Component.translatable(ProjectNublar.MODID + ".gui.photo.mode." + (this.userTab ? "global" : "user")));
            this.userTab = !this.userTab;
            ProjectNublar.NETWORK.sendToServer(new C2SPhotoBackgroundRequestAllIcons(!this.userTab));
        }, Supplier::get));
        this.openFileButton = this.add(ButtonAccessor.construct(x + 30, y + 110, 50, 20, Component.translatable(ProjectNublar.MODID + ".gui.photo.open"), b -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8("*.png"));
                filters.flip();
                String result = TinyFileDialogs.tinyfd_openFileDialog(I18n.get(ProjectNublar.MODID + ".gui.photo.upload.text"), null, filters, "PNG", false);
                if (result != null) {
                    this.setAsCurrent(Paths.get(result));
                }
            }
        }, Supplier::get));
        this.uploadButton = this.add(ButtonAccessor.construct(x + 130, y + 110, 50, 20, Component.translatable(ProjectNublar.MODID + ".gui.photo.upload"), b -> {
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
                ProjectNublar.LOGGER.error("Unable to write image", e);
            }
        }, Supplier::get));
        this.uploadButton.active = this.currentTestImage != null;
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
    public void onClose() {
        if(this.currentTestTexture != null) {
            this.currentTestTexture.close();
        }
        for (BGIconEntry entry : this.globalEntries) {
            if(entry.texture != null) {
                entry.texture.close();
            }
        }
        for (BGIconEntry userEntry : this.userEntries) {
            if(userEntry.texture != null) {
                userEntry.texture.close();
            }
        }
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
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.uploadButton.render(stack, mouseX, mouseY, partialTicks);
        this.globalChangeButton.render(stack, mouseX, mouseY, partialTicks);
        this.openFileButton.render(stack, mouseX, mouseY, partialTicks);

        if(this.currentTestTexture != null) {
            this.currentTestTexture.bind();
            RenderSystem.setShaderTexture(0, this.currentTestTexture.getId());
            blit(stack, x, y, 0, 0, 100, 100, 100, 100);
        }

        List<BGIconEntry> list = this.userTab ? this.userEntries : this.globalEntries;
        for (BGIconEntry entry : list) {
            if(entry.minY + entry.height + this.scroll - 5 < y + this.getHeight() && !entry.requested) {
                entry.requested = true;
                ProjectNublar.NETWORK.sendToServer(new C2SRequestPhotoBackgroundIcon(!this.userTab, entry.iconEntry.getUploaderUUID(), entry.iconEntry.getImageHash()));
            }
            if(entry.texture != null) {
                entry.texture.bind();
                RenderSystem.setShaderTexture(0, entry.texture.getId());
                blit(stack, entry.minX, entry.minY + this.scroll, 0, 0, entry.width, entry.height, entry.width, entry.height);
                if(entry == this.selectedEntry) {
                    stack.fill(entry.minX - 2, entry.minY + this.scroll - 2, entry.minX + entry.width + 2, entry.minY + entry.height + 2 + this.scroll, 0x8800BAFD);
                }
            }
        }
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    public void blit(GuiGraphics graphics, int x, int y, int uOffset, int vOffset, float width, float height, int textureWidth, int textureHeight) {
        this.blit(graphics, x, y, uOffset, vOffset, width, height, uOffset, vOffset, textureWidth, textureHeight);
    }
    public void blit(GuiGraphics graphics, int p_283671_, int p_282377_, int p_282058_, int p_281939_, float p_282285_, float p_283199_, int p_282186_, int p_282322_, int p_282481_, int p_281887_) {
        this.blit(graphics, p_283671_, p_283671_ + p_282058_, p_282377_, p_282377_ + p_281939_, 0, p_282186_, p_282322_, p_282285_, p_283199_, p_282481_, p_281887_);
    }

    void blit(GuiGraphics graphics, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        this.innerBlit(graphics, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float)p_282315_, (p_282660_ + (float)p_282193_) / (float)p_282315_, (p_281522_ + 0.0F) / (float)p_281436_, (p_281522_ + (float)p_281980_) / (float)p_281436_);
    }

    void innerBlit(GuiGraphics graphics, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float p_283247_, float p_282598_, float p_282883_, float p_283017_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, (float)p_281399_, (float)p_283615_, (float)p_281729_).uv(p_283247_, p_282883_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_281399_, (float)p_283430_, (float)p_281729_).uv(p_283247_, p_283017_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_283222_, (float)p_283430_, (float)p_281729_).uv(p_282598_, p_283017_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_283222_, (float)p_283615_, (float)p_281729_).uv(p_282598_, p_282883_).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
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

    @Override
    public void onFilesDrop(List<Path> files) {
        this.setAsCurrent(files.get(0));
        super.onFilesDrop(files);
    }

    private void setAsCurrent(Path path) {
        try {
            NativeImage image = NativeImage.read(Files.newInputStream(path));
            image = TextureUtils.resize(image, image.getWidth(), image.getHeight()); //If the user uploads a gif, it uploads ALL of the frames. This is to make sure it's just one frame
            if(this.currentTestTexture != null) {
                this.currentTestTexture.close();
            }
            this.currentTestTexture = new DynamicTexture(image);
            this.currentTestImage = image;
            this.uploadButton.active = true;
        } catch (IOException e) {
            ProjectNublar.LOGGER.error("Unable to read file " + path.toAbsolutePath() + " as an image", e);
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
    private static class BGIconEntry {

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
