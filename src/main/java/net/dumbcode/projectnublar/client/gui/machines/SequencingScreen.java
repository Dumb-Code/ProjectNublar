package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.gui.TextGuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class SequencingScreen extends SequencerSynthesizerBaseScreen {

    private static final ResourceLocation BASE_LOCATION = get("sequencer_page");

    private static final Component REGULAR = ProjectNublar.translate("gui.machine.sequencer.tab.sequencing.regular");
    private static final Component ISOLATED = ProjectNublar.translate("gui.machine.sequencer.tab.sequencing.isolated");

    private static final int OVERLAY_WIDTH = 472;
    private static final int OVERLAY_HEIGHT = 199;

    private static final float FIRST_SECTION_SIZE = 119F / 239F;

    private final List<DriveDisplayEntry> cachedDriveEntries = new ArrayList<>();
    private final List<IsolatedDriveEntry> cachedIsolatedEntries = new ArrayList<>();

    private final MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process;

    private GuiScrollBox<GuiScrollboxEntry> displayEntryScrollBox;
    private List<TextGuiScrollboxEntry> cachedIsolationEntries = new ArrayList<>();
    private GuiScrollBox<TextGuiScrollboxEntry> isolatedGeneScrollBox;

    private boolean showIsolatedGenes;
    private int cachedEntityAmount;
    private Entity cachedEntityRender;

    public SequencingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.process = blockEntity.getProcess(0);
        this.cacheEntries(blockEntity.getHandler().getStackInSlot(0));
    }

    @Override
    public void onSlotChanged(int slot, ItemStack stack) {
        if(slot == 0) {
            this.cacheEntries(stack);
        }
    }

    @Override
    public void init() {
        super.init();

        this.displayEntryScrollBox = this.addWidget(new GuiScrollBox<>(this.leftPos + 36, this.topPos + 42, 150, 13, 8, () ->
                (List<GuiScrollboxEntry>) (this.showIsolatedGenes ? this.cachedIsolatedEntries : this.cachedDriveEntries)
            ))
            .addFromPrevious(this.displayEntryScrollBox)
            .setBorderColor(0xFF577694)
            .setCellHighlightColor(0xCF193B59)
            .setCellSelectedColor(0xFF063B6B)
            .setInsideColor(0xFF193B59)
            .setEmptyColor(0xCF0F2234)
            .setRenderFullSize(true)
            .setShouldCountMouse(() -> this.activeSlot == null);

        this.isolatedGeneScrollBox = this.addWidget(new GuiScrollBox<>(this.leftPos + 195, this.topPos + 42, 150, 11, 9, () -> this.cachedIsolationEntries))
            .setActive(false)
            .addFromPrevious(this.isolatedGeneScrollBox)
            .setBorderColor(0xFF577694)
            .setCellHighlightColor(0xCF193B59)
            .setHighlightColor(0)
            .setInsideColor(0xFF193B59)
            .setEmptyColor(0xCF193B59)
            .setRenderFullSize(true)
            .setRenderCellBorders(false)
            .setShouldCountMouse(() -> this.activeSlot == null);

    }

    @Override
    public void containerTick() {
        super.containerTick();
        if(this.cachedEntityRender instanceof DinosaurEntity && Minecraft.getInstance().player.tickCount % 20 == 0) {
            DinosaurEntity e = (DinosaurEntity) this.cachedEntityRender;
            e.get(EntityComponentTypes.GENDER.get())
                .ifPresent(g -> g.male = !g.male);
        }
    }

    private void cacheEntries(ItemStack drive) {
        this.cachedDriveEntries.clear();
        this.cachedIsolatedEntries.clear();

        this.cachedIsolatedEntries.addAll(DriveUtils.getAllIsolatedGenes(drive).stream()
            .map(isolatedGene ->
                new IsolatedDriveEntry(
                    isolatedGene.getGeneticType().getTranslationComponent(),
                    isolatedGene.getProgress(),
                    isolatedGene.getParts()
                )
            )
            .collect(Collectors.toList())
        );

        this.cachedIsolatedEntries.sort(Comparator.<IsolatedDriveEntry, Double>comparing(d -> d.progress).reversed().thenComparing(d -> d.title.getString()));

        this.cachedDriveEntries.addAll(DriveUtils.getAll(drive).stream()
            .map(DriveDisplayEntry::new)
            .sorted(Comparator.comparing(e -> e.driveEntry.getKey()))
            .collect(Collectors.toList())
        );

        //If not showing iscoated genes, make sure the selection is transfered.
        GuiScrollboxEntry selectedElement = this.displayEntryScrollBox != null ? this.displayEntryScrollBox.getSelectedElement() : null;
        if(selectedElement instanceof DriveDisplayEntry) {
            DriveDisplayEntry selected = (DriveDisplayEntry) selectedElement;
            for (DriveDisplayEntry entry : this.cachedDriveEntries) {
                if(selected.driveEntry.getKey().equals(entry.driveEntry.getKey()) && Objects.equals(selected.driveEntry.getVariant(), entry.driveEntry.getVariant())) {
                    this.displayEntryScrollBox.setSelectedElement(entry);
                    break;
                }
            }
        }
    }

    @Override
    public void renderScreen(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        super.renderScreen(stack, mouseX, mouseY, ticks);
        drawProcessIcon(this.process, stack, 167.5F, 153);
        drawProcessTooltip(this.process, stack, 56, 151, 239, 19, mouseX, mouseY);

        if(this.cachedEntityRender instanceof DinosaurEntity && !this.showIsolatedGenes) {
            Dinosaur dinosaur = ((DinosaurEntity) this.cachedEntityRender).getDinosaur();


            RenderUtils.renderBorderExclusive(stack, this.leftPos + 210, this.topPos + 70, this.leftPos + 330, this.topPos + 140, 1, 0xFF577694);
            stack.fill(this.leftPos + 210, this.topPos + 70, this.leftPos + 330, this.topPos + 140, 0xCF193B59);

            stack.drawString(font, dinosaur.createNameComponent().withStyle(ChatFormatting.GOLD), this.leftPos + 213, this.topPos + 73, -1);
            stack.drawString(font, ProjectNublar.translate("dino.timeperiod.title").withStyle(Style.EMPTY.withUnderlined(true)), this.leftPos + 213, this.topPos + 82, -1);
            stack.drawString(font, ProjectNublar.translate("dino.timeperiod." + dinosaur.getDinosaurInfomation().getPeriod() + ".name").withStyle(ChatFormatting.AQUA), this.leftPos + 213, this.topPos + 92, -1);
            stack.drawString(font, ProjectNublar.translate("dino.diet.title").withStyle(Style.EMPTY.withUnderlined(true)), this.leftPos + 213, this.topPos + 104, -1);
            FeedingDiet diet = dinosaur.getAttacher().getStorage(ComponentHandler.METABOLISM.get()).getDiet();
            List<BlockState> blocks = diet.getBlocks();
            List<ItemStack> items = diet.getItems();
            List<EntityType<?>> entities = diet.getEntities();

            List<Component> component = new ArrayList<>();

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
                Component textComponent = component.get(i);
                if(i != component.size() - 1) {
                    textComponent = Component.literal("").append(textComponent).append(", ");
                }
                int width = font.width(textComponent);
                if (lineWidthSoFar + width >= 114) {
                    line++;
                    lineWidthSoFar = 0;
                }
                stack.drawString(font, textComponent, this.leftPos + 213 + lineWidthSoFar, this.topPos + 114 + 9 * line, -1);
                lineWidthSoFar += width;
            }

        }
    }

    @Override
    protected void renderBg(GuiGraphics stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        int regWidth = font.width(REGULAR);
        int regLeft = this.leftPos - regWidth + 174;

        int isoWidth = font.width(ISOLATED);
        int isoLeft = this.leftPos + 180;

        if(this.showIsolatedGenes) {
            stack.fill(isoLeft-1, this.topPos+21, isoLeft+isoWidth+1, this.topPos+23+font.lineHeight, 0x997777AA);
        } else {
            stack.fill(regLeft-1, this.topPos+21, regLeft+regWidth+1, this.topPos+23+font.lineHeight, 0x997777AA);
        }

        stack.drawString(font, REGULAR, regLeft, this.topPos + 22, -1);
        stack.drawString(font, ISOLATED, isoLeft, this.topPos + 22, -1);

        if(mouseY >= this.topPos + 22 && mouseY < this.topPos + 22 + font.lineHeight) {
            if(mouseX >= regLeft && mouseX < regLeft + regWidth) {
                stack.fill(regLeft-1, this.topPos+21, regLeft+regWidth+1, this.topPos+23+font.lineHeight, 0x2299bbff);
            }
            if(mouseX >= isoLeft && mouseX < isoLeft + isoWidth) {
                stack.fill(isoLeft-1, this.topPos+21, isoLeft+isoWidth+1, this.topPos+23+font.lineHeight, 0x2299bbff);
            }
        }

        stack.blit(BASE_LOCATION, this.leftPos, this.topPos, (int) stack.pose().last().pose().getTranslation(new Vector3f()).z, 0, 0, this.imageWidth, this.imageHeight, OVERLAY_HEIGHT, OVERLAY_WIDTH); //There is a vanilla bug that mixes up width and height

        float timeDone = this.process.getTimeDone();
        float leftDone = Mth.clamp(timeDone / FIRST_SECTION_SIZE, 0F, 1F);
        float rightDone = Mth.clamp((timeDone - FIRST_SECTION_SIZE) / (1 - FIRST_SECTION_SIZE), 0F, 1F);

        if(leftDone != 0) {
            subPixelBlit(stack, this.leftPos + 56, this.topPos + 151, 351, 0, leftDone * 119F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
        if(rightDone != 0) {
            subPixelBlit(stack, this.leftPos + 175, this.topPos + 151, 351, 19, rightDone * 120F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
//        stack.blit(this.leftPos, this.topPos);
        if(this.cachedEntityRender != null && !this.showIsolatedGenes && this.activeSlot == null) {
            int s = 50;
            int y = 100;
            if(this.cachedEntityRender instanceof DinosaurEntity) {
                s = 100;
                y = 50;
            }
            AABB box = this.cachedEntityRender.getBoundingBoxForCulling();
            double scale = s / box.getXsize();

            renderEntity(this.leftPos + 265, (int) (this.topPos + y + box.getYsize()/2*scale), scale, this.cachedEntityRender);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int regWidth = font.width(REGULAR);
        int regLeft = this.leftPos - regWidth + 174;
        int isoWidth = font.width(ISOLATED);
        int isoLeft = this.leftPos + 180;
        if(mouseY >= this.topPos + 22 && mouseY < this.topPos + 22 + font.lineHeight) {
            boolean ret = false;
            if(mouseX >= regLeft && mouseX < regLeft + regWidth) {
                this.showIsolatedGenes = false;
                this.isolatedGeneScrollBox.active = false;
                ret = true;
            }
            if(mouseX >= isoLeft && mouseX < isoLeft + isoWidth) {
                this.showIsolatedGenes = true;
                this.isolatedGeneScrollBox.active = true;
                ret = true;
            }
            if(ret) {
                displayEntryScrollBox.setSelectedElement(null);
                cachedEntityRender = null;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void renderEntity(int x, int y, double scale, Entity entity) {
//
//        float fogRed = FogRenderer.fogRed;
//        float fogGreen = FogRenderer.fogGreen;
//        float fogBlue = FogRenderer.fogBlue;
//
//        FogRenderer.fogRed = 255 / 255F ;
//        FogRenderer.fogGreen = 255 / 255F;
//        FogRenderer.fogBlue = 255 / 255F;
//
//        DriveDisplayEntry element = this.displayEntryScrollBox.getSelectedElement();
//        int amount = 100;
//        if(element != null) {
//            amount = element.driveEntry.getAmount();
//        }
//        boolean fog = false;// amount < 50;
//        if(fog) {
//            float p = 1 - amount / 50F;
//            float range = 1000000;
//
//            RenderSystem.enableFog();
//            RenderSystem.fogStart(-range*p);
//            RenderSystem.fogEnd(range - range*p);
//            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
//            RenderSystem.setupNvFogDistance();
//        }

        boolean rotateUp = entity.getType() == EntityType.COD || entity.getType() == EntityType.SALMON || entity.getType() == EntityType.TROPICAL_FISH;
        Quaternionf quaternion = Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getFrameTime());
        quaternion.mul(rotateUp ? Axis.ZN.rotationDegrees(-90) : new Quaternionf(1, 1, 1, 1));

        renderEntityAt(x, y, scale, entity, 1000, quaternion);

//        renderEntityAt(x, y, scale, entity);

//        if(fog) {
//            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
//            RenderSystem.disableFog();
//        }
//
//
//        FogRenderer.fogRed = fogRed;
//        FogRenderer.fogGreen = fogGreen;
//        FogRenderer.fogBlue = fogBlue;
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
                Optional<EntityType<?>> type = entry.getEntity();
                if(type.isPresent()) {
                    entity = EntityGeneticRegistry.INSTANCE.createFromType(type.get(), Minecraft.getInstance().level, entry.getVariant());
                }
                break;
        }
        this.cachedEntityRender = entity;
        this.cachedEntityAmount = entry.getAmount();
    }

    @RequiredArgsConstructor
    private class DriveDisplayEntry implements GuiScrollboxEntry {

        @Getter
        private final DriveUtils.DriveEntry driveEntry;

        @Override
        public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            MutableComponent component = this.driveEntry.getTranslation().append(": " + this.driveEntry.getAmount() + "%");
            Font font = Minecraft.getInstance().font;
            stack.drawString(font, component.toString(), x + (150 - font.width(component)) / 2F, y + 3, -1, false);
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

    private class IsolatedDriveEntry implements GuiScrollboxEntry {

        private final MutableComponent title;
        private final double progress;
        private final List<Pair<MutableComponent, Double>> entityAmounts;

        private IsolatedDriveEntry(MutableComponent title, double progress, List<Pair<MutableComponent, Double>> entityAmounts) {
            this.title = title;
            this.progress = progress;
            this.entityAmounts = entityAmounts;
            this.entityAmounts.sort(Comparator.<Pair<MutableComponent, Double>, Double>comparing(Pair::getSecond).reversed().thenComparing(d -> d.getFirst().getString()));
        }

        @Override
        public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            stack.drawString(font, this.title.append(": " + Mth.floor(this.progress * 100D) + "%"), x + 2, y + 3, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            if(displayEntryScrollBox.getSelectedElement() == this) {
                displayEntryScrollBox.setSelectedElement(null);
                cachedIsolationEntries.clear();
                return false;
            }
            cachedIsolationEntries.clear();
            cachedIsolationEntries.addAll(
                this.entityAmounts.stream()
                    .map(p -> new TextGuiScrollboxEntry(p.getFirst().copy().append(": " + Mth.floor(p.getSecond() * 100D) + "%")))
                    .collect(Collectors.toList())
            );
            isolatedGeneScrollBox.setScroll(0);
            return true;
        }
    }
}
