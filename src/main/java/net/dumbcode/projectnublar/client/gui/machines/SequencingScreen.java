package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SequencingScreen extends SequencerSynthesizerBaseScreen {

    private static final ResourceLocation BASE_LOCATION = get("sequencer_page");

    private static final int OVERLAY_WIDTH = 472;
    private static final int OVERLAY_HEIGHT = 199;

    private static final float FIRST_SECTION_SIZE = 119F / 239F;

    private final Supplier<List<DriveUtils.DriveEntry>> driveEntriesGetter;
    private final List<DriveDisplayEntry> cachedEntries = new ArrayList<>();

    private final MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process;

    private GuiScrollBox<DriveDisplayEntry> displayEntryScrollBox;

    private int cachedEntityAmount;
    private Entity cachedEntityRender;

    public SequencingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.process = blockEntity.getProcess(0);
        this.driveEntriesGetter = () -> DriveUtils.getAll(blockEntity.getHandler().getStackInSlot(0));
    }

    @Override
    public void init() {
        super.init();
        this.cacheEntries();

        this.displayEntryScrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 66, this.topPos + 29, 150, 13, 9, () -> this.cachedEntries))
            .addFromPrevious(this.displayEntryScrollBox).setBorderColor(0xFF577694).setCellHighlightColor(0xCF193B59).setCellSelectedColor(0xFF063B6B).setInsideColor(0xFF193B59).setEmptyColor(0xCF0F2234).setRenderFullSize(true).setShouldCountMouse(() -> this.activeSlot == null);
    }

    @Override
    public void tick() {
        this.cacheEntries();
        super.tick();
        if(this.cachedEntityRender instanceof DinosaurEntity && Minecraft.getInstance().player.tickCount % 20 == 0) {
            DinosaurEntity e = (DinosaurEntity) this.cachedEntityRender;
            e.get(EntityComponentTypes.GENDER.get())
                .ifPresent(g -> g.male = !g.male);
        }
    }

    private void cacheEntries() {
        DriveDisplayEntry selectedElement = this.displayEntryScrollBox != null ? this.displayEntryScrollBox.getSelectedElement() : null;
        this.cachedEntries.clear();
        this.cachedEntries.addAll(this.driveEntriesGetter.get().stream()
                .map(DriveDisplayEntry::new)
                .sorted(Comparator.comparing(e -> e.driveEntry.getKey()))
                .collect(Collectors.toList())
        );
        if(selectedElement != null) {
            for (DriveDisplayEntry entry : this.cachedEntries) {
                if(entry.driveEntry.getKey().equals(selectedElement.driveEntry.getKey())) {
                    this.displayEntryScrollBox.setSelectedElement(entry);
                    break;
                }
            }
        }
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        super.renderScreen(stack, mouseX, mouseY, ticks);
        drawProcessIcon(this.process, stack, 167.5F, 153);
        drawProcessTooltip(this.process, stack, 56, 151, 239, 19, mouseX, mouseY);

        if(this.cachedEntityRender instanceof DinosaurEntity) {
            Dinosaur dinosaur = ((DinosaurEntity) this.cachedEntityRender).getDinosaur();


            RenderUtils.renderBorderExclusive(stack, this.leftPos + 230, this.topPos + 70, this.leftPos + 330, this.topPos + 140, 1, 0xFF577694);
            fill(stack, this.leftPos + 230, this.topPos + 70, this.leftPos + 330, this.topPos + 140, 0xCF193B59);

            drawString(stack, font, dinosaur.createNameComponent().withStyle(TextFormatting.GOLD), this.leftPos + 233, this.topPos + 73, -1);
            drawString(stack, font, ProjectNublar.translate("dino.timeperiod.title").withStyle(Style.EMPTY.withUnderlined(true)), this.leftPos + 233, this.topPos + 82, -1);
            drawString(stack, font, ProjectNublar.translate("dino.timeperiod." + dinosaur.getDinosaurInfomation().getPeriod() + ".name").withStyle(TextFormatting.AQUA), this.leftPos + 233, this.topPos + 92, -1);
            drawString(stack, font, ProjectNublar.translate("dino.diet.title").withStyle(Style.EMPTY.withUnderlined(true)), this.leftPos + 233, this.topPos + 104, -1);
            FeedingDiet diet = dinosaur.getAttacher().getStorage(ComponentHandler.METABOLISM.get()).getDiet();
            List<BlockState> blocks = diet.getBlocks();
            List<ItemStack> items = diet.getItems();
            List<EntityType<?>> entities = diet.getEntities();

            List<ITextComponent> component = new ArrayList<>();

            for(int i = 0; component.size() < 3 && (i < blocks.size() || i < items.size() || i < entities.size()); i++) {
                if(i < blocks.size()) {
                    component.add(blocks.get(i).getBlock().getName());
                }
                if(i < items.size()) {
                    component.add(items.get(i).getHoverName());
                }
                if(i < entities.size()) {
                    component.add(entities.get(i).getDescription());
                }
            }

            int lineWidthSoFar = 0;
            int line = 0;
            for (int i = 0; i < component.size(); i++) {
                ITextComponent textComponent = component.get(i);
                if(i != component.size() - 1) {
                    textComponent = new StringTextComponent("").append(textComponent).append(", ");
                }
                int width = font.width(textComponent);
                if (lineWidthSoFar + width >= 64) {
                    line++;
                    lineWidthSoFar = 0;
                }
                drawString(stack, font, textComponent, this.leftPos + 233 + lineWidthSoFar, this.topPos + 114 + 9 * line, -1);
                lineWidthSoFar += width;
            }

        }
    }

    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, OVERLAY_HEIGHT, OVERLAY_WIDTH); //There is a vanilla bug that mixes up width and height

        float timeDone = this.process.getTimeDone();
        float leftDone = MathHelper.clamp(timeDone / FIRST_SECTION_SIZE, 0F, 1F);
        float rightDone = MathHelper.clamp((timeDone - FIRST_SECTION_SIZE) / (1 - FIRST_SECTION_SIZE), 0F, 1F);

        if(leftDone != 0) {
            subPixelBlit(stack, this.leftPos + 56, this.topPos + 151, 351, 0, leftDone * 119F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
        if(rightDone != 0) {
            subPixelBlit(stack, this.leftPos + 175, this.topPos + 151, 351, 19, rightDone * 120F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
//        blit(stack, this.leftPos, this.topPos);
        if(this.cachedEntityRender != null) {
            int s = 50;
            int y = 100;
            if(this.cachedEntityRender instanceof DinosaurEntity) {
                s = 100;
                y = 50;
            }
            AxisAlignedBB box = this.cachedEntityRender.getBoundingBoxForCulling();
            double scale = s / box.getXsize();

            renderEntity(this.leftPos + 280, (int) (this.topPos + y + box.getYsize()/2*scale), scale, this.cachedEntityRender);
        }
    }

    private void renderEntity(int x, int y, double scale, Entity entity) {

        float fogRed = FogRenderer.fogRed;
        float fogGreen = FogRenderer.fogGreen;
        float fogBlue = FogRenderer.fogBlue;

        FogRenderer.fogRed = 255 / 255F ;
        FogRenderer.fogGreen = 255 / 255F;
        FogRenderer.fogBlue = 255 / 255F;

        DriveDisplayEntry element = this.displayEntryScrollBox.getSelectedElement();
        int amount = 100;
        if(element != null) {
            amount = element.driveEntry.getAmount();
        }
        boolean fog = false;// amount < 50;
        if(fog) {
            float p = 1 - amount / 50F;
            float range = 1000000;

            RenderSystem.enableFog();
            RenderSystem.fogStart(-range*p);
            RenderSystem.fogEnd(range - range*p);
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            RenderSystem.setupNvFogDistance();
        }

        renderEntityAt(x, y, scale, entity);

        if(fog) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
            RenderSystem.disableFog();
        }


        FogRenderer.fogRed = fogRed;
        FogRenderer.fogGreen = fogGreen;
        FogRenderer.fogBlue = fogBlue;
    }

    private void selectDriveEntry(DriveUtils.DriveEntry entry) {
        Entity entity = null;
        switch (entry.getDriveType()) {
            case DINOSAUR:
                Dinosaur dinosaur = DinosaurHandler.getRegistry().getValue(new ResourceLocation(entry.getKey()));
                if(dinosaur != null) {
                    entity = dinosaur.createEntity(
                        Minecraft.getInstance().level,
                        dinosaur.getAttacher()
                            .getDefaultConfig()
                            .runBeforeFinalize(EntityComponentTypes.GENETICS.get(), GeneticComponent::useExistingGenetics)
                    );
                }
                break;
            case OTHER:
                EntityType<?> value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entry.getKey()));
                if(value != null) {
                    entity = value.create(Minecraft.getInstance().level);
                }
                break;
        }
        this.cachedEntityRender = entity;
        this.cachedEntityAmount = entry.getAmount();
    }

    @RequiredArgsConstructor
    private class DriveDisplayEntry implements GuiScrollboxEntry {

        private final DriveUtils.DriveEntry driveEntry;

        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            IFormattableTextComponent component = this.driveEntry.getTranslation().append(": " + this.driveEntry.getAmount() + "%");
            FontRenderer font = Minecraft.getInstance().font;
            font.draw(stack, component, x + (150 - font.width(component)) / 2F, y + 3, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            if(displayEntryScrollBox.getSelectedElement() == this) {
                displayEntryScrollBox.setSelectedElement(null);
                cachedEntityRender = null;
                return false;
            }
            selectDriveEntry(this.driveEntry);
            return true;
        }

    }
}
