package net.dumbcode.projectnublar.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import sun.awt.shell.ShellFolder;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GuiFileExplorer extends GuiScreen {

    @Nullable private final GuiScreen parent;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final File root;
    private String superseedingFolder = ""; //TODO: Why not just make this a File?

    private final List<GuiFileEntry> entries = Lists.newArrayList();
    private final List<GuiFileEntry> searchedEntries = Lists.newArrayList();

    private final String buttonText;
    private final Consumer<File> fileConsumer;

    private GuiFileList fileList;
    private GuiTextField fileName;
    private GuiButton selectButton;
    private GuiButton actionButton;
    private GuiButton addFolderButton;
    private int selectedIndex = -1;

    private final ResourceLocation folderLoc;

    private static final Map<String, ResourceLocation> IMAGE_CACHE = Maps.newHashMap();

    @SneakyThrows
    public GuiFileExplorer(@Nullable GuiScreen parent, String root, String buttonText, Consumer<File> fileConsumer) {
        this.parent = parent;
        this.root = new File(mc.gameDir, root);
        this.buttonText = buttonText;
        this.fileConsumer = fileConsumer;
        if(!this.root.exists() && !this.root.mkdirs() ) {
            ProjectNublar.getLogger().error("Unable to load directory: " + this.root.getAbsoluteFile());
        }
        this.updateEntries();

        //An empty string is the folder
        if(IMAGE_CACHE.containsKey("")) {
            this.folderLoc = IMAGE_CACHE.get("");
        } else {
            IMAGE_CACHE.put("", this.folderLoc = this.mc.renderEngine.getDynamicTextureLocation("", new DynamicTexture(getIcon(this.mc.gameDir))));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.fileList = new GuiFileList();
        this.fileName = new GuiTextField(0, mc.fontRenderer, this.width / 2 - 100, this.height - 25, 200, 20);
        this.fileName.setFocused(true);
        this.fileName.setCanLoseFocus(false);
        this.selectButton = this.addButton(new GuiButton(1, 5, this.height - 25, this.width / 2 - 110, 20, I18n.format("gui.button.selectfile")));
        this.actionButton = this.addButton(new GuiButton(2, this.width / 2 + 105, this.height - 25, this.width / 2 - 110, 20, I18n.format(this.buttonText)));
        this.addFolderButton = this.addButton(new GuiButton(3, this.width - 25, 5, 20, 20, ""));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.fileList.drawScreen(mouseX, mouseY, partialTicks);
        this.fileName.drawTextBox();
        this.selectButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        this.actionButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        this.addFolderButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        this.mc.renderEngine.bindTexture(this.folderLoc);
        Gui.drawModalRectWithCustomSizedTexture(this.addFolderButton.x + this.addFolderButton.height / 2 - 8, this.addFolderButton.y + this.addFolderButton.width / 2 - 8, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void updateScreen() {
        this.fileName.updateCursorCounter();
        this.actionButton.enabled = this.selectedIndex == -1 ? !this.fileName.getText().trim().isEmpty() : this.searchedEntries.get(this.selectedIndex).file.isFile();
        this.addFolderButton.enabled = !this.fileName.getText().isEmpty();
    }

    private void updateEntries() {
        this.entries.clear();
        boolean onRoot = this.superseedingFolder.isEmpty();
        File rootFile = onRoot ? this.root : new File(this.root, this.superseedingFolder);
        if(!onRoot) {
            this.entries.add(new GuiFileEntry(rootFile.getParentFile(), true));
        }
        List<File> folders = Lists.newArrayList();
        List<File> files = Lists.newArrayList();
        for (File file : rootFile.listFiles()) {
            (file.isDirectory() ? folders : files).add(file);
        }
        folders.sort(Comparator.comparing(File::getName));
        files.sort(Comparator.comparing(File::getName));
        for (File folder : folders) {
            this.entries.add(new GuiFileEntry(folder));
        }
        for (File file : files) {
            this.entries.add(new GuiFileEntry(file));
        }
        this.updateSearchedEntries();
    }

    private void updateSearchedEntries() {
        this.searchedEntries.clear();
        String search = this.fileName == null ? "" : this.fileName.getText().trim();
        if(search.isEmpty()) {
            this.searchedEntries.addAll(this.entries);
        } else {
            for (GuiFileEntry entry : this.entries) {
                if(entry.file.getName().startsWith(search) || entry.isParent) {
                    this.searchedEntries.add(entry);
                }
            }
        }
        if(this.searchedEntries.isEmpty()) {
            this.searchedEntries.addAll(this.entries);
        }
        this.selectedIndex = -1;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(this.parent);
        } else {
            this.fileName.textboxKeyTyped(typedChar, keyCode);
            this.updateSearchedEntries();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        this.fileList.actionPerformed(button);
        if(button == this.selectButton) {
            if(this.selectedIndex != -1) {
                this.selectEntry(this.selectedIndex);
            }
        } else if(button == this.actionButton && this.actionButton.enabled /*Just make sure that the button is enabled. It should be virtually impossible for it not to be*/) {
            File out = this.selectedIndex == -1 ? new File(this.root, this.superseedingFolder + "/" + this.fileName.getText().trim()) : this.searchedEntries.get(this.selectedIndex).file;
            fileConsumer.accept(out);
            this.mc.displayGuiScreen(this.parent);
        } else if (button == this.addFolderButton && new File(this.root, this.superseedingFolder + "/" + this.fileName.getText().trim()).mkdirs()) {
            this.fileName.setText("");
            this.updateEntries();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.fileList.handleMouseInput();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.fileList.mouseReleased(mouseX, mouseY, state);
    }

    private void selectEntry(int index) {
        GuiFileEntry entry = this.searchedEntries.get(index);
        if(entry.isParent) {
            this.superseedingFolder = this.superseedingFolder.substring(0, this.superseedingFolder.lastIndexOf("/"));
        } else if(entry.file.isDirectory()){
            this.superseedingFolder += "/" + entry.file.getName();
        } else {
            this.fileName.setText(entry.file.getName());
        }
        GuiFileExplorer.this.updateEntries();
    }

    private class GuiFileList extends GuiListExtended {

        private GuiFileList() {
            super(GuiFileExplorer.this.mc, GuiFileExplorer.this.width, GuiFileExplorer.this.height, 32, GuiFileExplorer.this.height - 32, 20);
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return GuiFileExplorer.this.searchedEntries.get(index);
        }

        @Override
        protected int getSize() {
            return GuiFileExplorer.this.searchedEntries.size();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == GuiFileExplorer.this.selectedIndex;
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            if(isDoubleClick) {
                GuiFileExplorer.this.selectEntry(slotIndex);
            } else {
                GuiFileExplorer.this.selectedIndex = slotIndex;
            }
            super.elementClicked(slotIndex, isDoubleClick, mouseX, mouseY);
        }
    }

    public static BufferedImage getIcon(File file) throws FileNotFoundException {
        Image icon = ShellFolder.getShellFolder(file).getIcon(true); //Test usability on other os
        BufferedImage im = new BufferedImage(icon.getWidth(null), icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.drawImage(icon, 0, 0, null);
        g.dispose();
        return im;
    }

    private class GuiFileEntry implements GuiListExtended.IGuiListEntry {

        private final File file;
        private final boolean isParent;
        private ResourceLocation location;

        private GuiFileEntry(File file) {
            this(file, false);
        }

        private GuiFileEntry(File file, boolean isParent) {
            this.file = file;
            this.isParent = isParent;

            String fileExtension = FilenameUtils.getExtension(file.getName());
            try {
                ResourceLocation location = new ResourceLocation(ProjectNublar.MODID, "textures/gui/file-explorer/custom-extensions/" + fileExtension + ".png");
                mc.getResourceManager().getResource(location);
                this.location = location;
            } catch (IOException ignored) { //If something goes wrong with loading the file (if its not there or coruppted
                if(IMAGE_CACHE.containsKey(fileExtension)) {
                    this.location =  IMAGE_CACHE.get(fileExtension);
                } else {
                    ResourceLocation location = null;
                    try {
                        IMAGE_CACHE.put(fileExtension, location = mc.renderEngine.getDynamicTextureLocation(FilenameUtils.getExtension(file.getName()), new DynamicTexture(getIcon(this.file))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    this.location = location == null ? new ResourceLocation(ProjectNublar.MODID, "textures/gui/file-explorer/unknownfile.png") : location;
                }
            }
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            mc.renderEngine.bindTexture(this.location);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
            GuiFileExplorer.this.mc.fontRenderer.drawStringWithShadow(this.isParent ? ".." : this.file.getName(), x + 25, y + slotHeight / 2 - 4, -1);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

        }
    }
}
