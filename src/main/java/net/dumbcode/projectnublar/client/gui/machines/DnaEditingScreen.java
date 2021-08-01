package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C2SSequencingSynthesizerSelectChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DnaEditingScreen extends SequencerSynthesizerBaseScreen {

    private final SequencingSynthesizerBlockEntity blockEntity;

    //TODO: make colors entity related.
    private final int[] colors ={ 0x2acc9b, 0xa0dd66, 0x23e0e3, 0xe30394, 0x1d52f5, 0x484179, 0x4f985a, 0x680fb7 };

    private final List<DnaSelectModuleSlot> slotList = Lists.newArrayList();

    private final List<DriveEntry> dinosaurList = Lists.newArrayList();
    private final List<DriveEntry> entryList = Lists.newArrayList();
    private final List<DriveEntry> filteredListForSlot = Lists.newArrayList();

    private final List<GeneticEntry<?, ?>> combineGenetics = Lists.newArrayList();
    private final List<GuiScrollboxEntry> combinedEditGeneList = Lists.newArrayList();
    private List<GuiScrollboxEntry> individualBreakdownList;

    private int slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
    private int selectedSlot = -1;
    private float renderTicks;

    private DinosaurEntity cachedEntity;

    private GuiScrollBox<DriveEntry> scrollBox;
    private GuiScrollBox<GuiScrollboxEntry> geneScrollBox;
    private IFormattableTextComponent hoveringText;

    public DnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
        this.refreshGeneList();
    }


    @Override
    public void init() {
        super.init();

        this.slotList.clear();
        for (int i = 0; i < SequencingSynthesizerBlockEntity.TOTAL_SLOTS; i++) {
            this.slotList.add(this.addButton(new DnaSelectModuleSlot(this.slotList.size(), this.leftPos + 10, 0)));
        }
        this.updateAllSlots();

        this.updateLists();

        this.scrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 232, this.topPos + 23, 109, 20, 5, () -> {
            if(this.selectedSlot == -1) {
                return Collections.emptyList();
            } else if(this.selectedSlot == 0) {
                return this.dinosaurList;
            } else {
                return this.filteredListForSlot;
            }
        }).addFromPrevious(this.scrollBox).setBorderColor(0xFF577694).setCellHighlightColor(0xCF193B59).setCellSelectedColor(0xFF06559C).setInsideColor(0xFF193B59).setEmptyColor(0xCF0F2234).setRenderFullSize(true));

        this.geneScrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 104, this.topPos + 134, 237, 14, 3, () -> this.individualBreakdownList != null ? this.individualBreakdownList : this.combinedEditGeneList)
            .addFromPrevious(this.geneScrollBox).setCellsPerRow(2).setBorderColor(0xFF577694).setCellHighlightColor(0xCF193B59).setCellSelectedColor(0xFF06559C).setInsideColor(0xFF193B59).setEmptyColor(0xCF0F2234).setRenderFullSize(true));
    }

    @Override
    public void tick() {
        super.tick();
        this.updateLists();

        int slots = this.slots;
        this.slots = this.blockEntity.getSlots();
        if(slots != this.slots) {
            this.updateAllSlots();
        }

        if(this.cachedEntity != null && Minecraft.getInstance().player.tickCount % 20 == 0) {
            this.cachedEntity.get(EntityComponentTypes.GENDER.get()).ifPresent(g -> g.male = true);
        }
    }

    private void updateLists() {
        List<DriveEntry> safeEntries = Lists.newArrayList();

        for (DriveUtils.DriveEntry entry : DriveUtils.getAll(this.blockEntity.getHandler().getStackInSlot(0))) {
            DriveEntry match = null;
            for (DriveEntry driveEntry : this.entryList) {
                if(driveEntry.key.equals(entry.getKey())) {
                    match = driveEntry;
                    break;
                }
            }
            if (match == null) {
                DriveEntry e = new DriveEntry(entry.getKey(), entry, entry.getTranslation());
                if (entry.getDriveType() == DriveUtils.DriveType.DINOSAUR) {
                    this.dinosaurList.add(e);
                }
                this.entryList.add(e);
                safeEntries.add(e);
            } else {
                safeEntries.add(match);
            }
        }
        this.updateDriveEntires(safeEntries);
        this.refreshFilteredList();

        for (DnaSelectModuleSlot moduleSlot : this.slotList) {
            String key = this.blockEntity.getSelectKey(moduleSlot.id);
            double amount = this.blockEntity.getSelectAmount(moduleSlot.id);

            DriveEntry foundEntry = null;
            for (DriveEntry entry : this.entryList) {
                if(entry.key.equals(key)) {
                    foundEntry = entry;
                    break;
                }
            }
            moduleSlot.setDrive(foundEntry);
            moduleSlot.slider.setValueDirectly(amount);
        }
        this.refreshEntity();
        this.refreshGeneList();
//        this.refreshGeneticsOnEntity();
    }

    private void updateDriveEntires(List<DriveEntry> safeEntries) {
        for (DriveEntry e : this.entryList) {
            if(safeEntries.contains(e)) {
                continue;
            }

            for (DnaSelectModuleSlot module : this.slotList) {
                if(module.drive == e) {
                    module.setDrive(null);
                }
            }

            this.dinosaurList.remove(e);
        }
        this.entryList.removeIf(d -> !safeEntries.contains(d));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        hoveringText = null;
        super.render(stack, mouseX, mouseY, ticks);
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderScreen(stack, mouseX, mouseY, partialTicks);

//        int xStart = this.leftPos + 102;
//        int yStart = this.topPos + 166;
//
//        int w = 128;
//        int yEnd = yStart + 10;
//
//        fill(stack, xStart-1, yStart-1, xStart+w+1, yEnd+1, -1);
//
//        int start = 0;
//        for (DnaSelectModuleSlot module : this.slotList) {
//            if(module.drive == null || module.slider.getSliderValue() == 0) {
//                continue;
//            }
//            int value = (int) Math.round((w+1) * MathHelper.clamp(module.slider.getSliderValue(), 0D, 1D));
//            fill(stack, xStart + start, yStart, xStart + start + value-1, yEnd, 0xFF000000|this.colors[module.id]);
//            if(module.drive != null && mouseX >= xStart + start && mouseX < xStart + start + value && mouseY >= yStart && mouseY < yEnd) {
//                hoveringText = module.drive.entry;
//            }
//            start += value;
//
//        }
//        if(start < w) {
//            fill(stack, xStart + start, yStart, xStart + w, yEnd, 0xFF000000);
//        }


        if(hoveringText != null) {
            int width = minecraft.font.width(hoveringText);
            fill(stack, mouseX+5, mouseY-1, mouseX + width + 6, mouseY + minecraft.font.lineHeight+1, 0xFF23374A);
            RenderUtils.renderBorderExclusive(stack, mouseX+5, mouseY-1, mouseX + width +6, mouseY + minecraft.font.lineHeight+1, 1, 0xFF577694);
            drawString(stack, minecraft.font, hoveringText, mouseX + 6, mouseY, -1);
        }
        renderTooltip(stack, mouseX, mouseY);
        this.renderTicks += partialTicks;

        if(this.cachedEntity != null) {
            AxisAlignedBB a = this.cachedEntity.getBoundingBoxForCulling();
            double min = Math.max(a.getXsize(), Math.max(a.getYsize(), a.getZsize()));

            fill(stack, this.leftPos+105, this.topPos+24, this.leftPos+223, this.topPos+123, 0xAF193B59);
            RenderUtils.renderBorder(stack, this.leftPos+104, this.topPos+23, this.leftPos+224, this.topPos+124, 1, 0xFF577694);

            StencilStack.pushSquareStencil(stack, this.leftPos+105, this.topPos+24, this.leftPos+223, this.topPos+123);
            renderEntityAndPlayer(this.leftPos + 167, this.topPos + 110, 200 / min, this.cachedEntity, mouseX, mouseY);
            StencilStack.popStencil();
        }
    }

    private static void renderEntityAndPlayer(int x, int y, double scale, Entity entity, double mouseX, double mouseY) {
        renderEntityAt(x, y, scale, entity);

        ClientPlayerEntity player = Minecraft.getInstance().player;
        double mx = x + 20 - mouseX;
        double my = y - scale*1.5 - mouseY;

        float angleX = (float)Math.atan(mx / 40.0F);
        float angleY = (float)Math.atan(my / 40.0F);

        float yBodyRot = player.yBodyRot;
        float yRot = player.yRot;
        float xRot = player.xRot;
        float yHeadRotO = player.yHeadRotO;
        float yHeadRot = player.yHeadRot;

        player.yBodyRot = 180.0F + angleX * 20.0F;;
        player.yRot = 180.0F + angleX * 40.0F;;
        player.xRot = -angleY * 20.0F;;
        player.yHeadRot = player.yRot;
        player.yHeadRotO = player.yRot;

        renderEntityAt(x + 20, y, scale, player, 900, Vector3f.XP.rotationDegrees(angleY * 20.0F));

        player.yBodyRot = yBodyRot;
        player.yRot = yRot;
        player.xRot = xRot;
        player.yHeadRotO = yHeadRotO;
        player.yHeadRot = yHeadRot;
    }

    private void setSelectedSlot(int selectedSlot) {
        if(this.selectedSlot == selectedSlot) {
            selectedSlot = -1;
            this.scrollBox.setSelectedElement(null);
        }
        this.selectedSlot = selectedSlot;
        this.refreshFilteredList();

        if(selectedSlot != -1) {
            DriveEntry drive = this.slotList.get(selectedSlot).getDrive();
            if(drive != null) {
                String key = drive.getKey();
                for (DriveEntry entry : this.filteredListForSlot) {
                    if(entry.key.equals(key)) {
                        scrollBox.setSelectedElement(entry);
                        break;
                    }
                }
            }

        }
    }

    private void refreshFilteredList() {
        if(this.selectedSlot != -1) {
            this.filteredListForSlot.clear();
            this.filteredListForSlot.addAll(this.entryList);
            for (int i = 0; i < this.selectedSlot; i++) {
                DriveEntry drive = this.slotList.get(i).getDrive();
                if(drive != null) {
                    String key = drive.getKey();
                    this.filteredListForSlot.removeIf(p -> p.key.equals(key));
                }
            }

            this.individualBreakdownList = new ArrayList<>();
            DnaSelectModuleSlot slot = this.slotList.get(this.selectedSlot);
            DriveEntry entry = slot.drive;
            if(entry != null && entry.driveEntry.getDriveType() == DriveUtils.DriveType.OTHER) {
                ResourceLocation location = new ResourceLocation(entry.getKey());
                if(ForgeRegistries.ENTITIES.containsKey(location)) {
                    EntityType<?> value = ForgeRegistries.ENTITIES.getValue(location);
                    for (EntityGeneticRegistry.Entry<?, ?> e : EntityGeneticRegistry.INSTANCE.gatherEntry(value, entry.driveEntry.getVariant())) {
                        this.individualBreakdownList.add(new GeneEditEntry<>(e.create((float) slot.slider.getSliderValue())));
                    }

                    SequencingSynthesizerBlockEntity.DnaColourStorage storage = this.blockEntity.getStorage(this.selectedSlot);
                    if(storage != null) {
                        List<Integer> tints = EntityGeneticRegistry.INSTANCE.gatherTints(value, entry.driveEntry.getVariant());
                        for (int i = 0; i < tints.size(); i++) {
                            this.individualBreakdownList.add(new GeneEditTintEntry(i, tints.get(i), storage));
                        }
                    }
                }
            }

        } else {
            this.individualBreakdownList = null;
        }
    }

    private void refreshGeneList() {
        this.combinedEditGeneList.clear();
        this.combineGenetics.clear();
        this.combineGenetics.addAll(GeneticUtils.combineAll(this.blockEntity.gatherAllGeneticEntries()));
        for (GeneticEntry<?, ?> entry : this.combineGenetics) {
            this.combinedEditGeneList.add(new GeneEditEntry<>(entry));
        }
        this.refreshGeneticsOnEntity();
    }

    private void updateAllSlots() {
        int center = this.topPos + this.imageHeight / 2 + 1;
        int totalHeight = 18*this.slots - 2; //4 is the padding on the bottom element we take away.
        int yStart = center - totalHeight / 2;
        for (DnaSelectModuleSlot slot : this.slotList) {
            slot.visible = slot.id < this.slots;
            if(slot.visible) {
                slot.y = yStart + slot.id*18;
                slot.slider.y = slot.y;
            }
        }
    }

    private void changeDnaSelection(int id, String key, double amount) {
        if (this.blockEntity.setAndValidateSelect(id, key, amount)) {
            ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                id, key, amount,
                this.blockEntity.getStorage(id)
            ));
            if(id == 0) {
                this.refreshEntity();
            }
            this.refreshGeneList();
        }
    }

    private void refreshEntity() {
        DnaSelectModuleSlot slot = this.slotList.get(0);
        boolean hasDinosaur = !slot.drive.key.isEmpty() && slot.slider.getSliderValue() != 0;
        if(hasDinosaur) {
            Dinosaur dinosaur = DinosaurHandler.getRegistry().getValue(new ResourceLocation(slot.drive.key));
            if(this.cachedEntity == null || this.cachedEntity.getDinosaur() != dinosaur) {
                this.cachedEntity = dinosaur.createEntity(
                    Minecraft.getInstance().level,
                    dinosaur.getAttacher().getDefaultConfig()
                        .runBeforeFinalize(EntityComponentTypes.GENETICS.get(), GeneticComponent::disableRandomGenetics)
                );
            }
        } else if(this.cachedEntity != null) {
            this.cachedEntity = null;
        }
    }

    private void refreshGeneticsOnEntity() {
        if(this.cachedEntity != null) {
            this.cachedEntity.get(EntityComponentTypes.GENETICS).ifPresent(g -> GeneticComponent.replaceGenetics(g, this.cachedEntity, this.combineGenetics));
        }
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        for (DnaSelectModuleSlot module : this.slotList) {
            module.slider.mouseReleased(mouseX, mouseY, state);
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }


    private class DnaSelectModuleSlot extends Widget implements INestedGuiEventHandler {

        @Nullable
        private IGuiEventListener focused;
        private boolean isDragging;

        private final int id;
        private final ClampedGuiSlider slider;
        @Getter
        private DriveEntry drive;

        private DnaSelectModuleSlot(int id, int x, int y) {
            super(x, y, 109, 16, new StringTextComponent(":)"));
            this.id = id;
            this.slider = new ClampedGuiSlider(id, x + 16, y, this);
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
            if(!this.visible) {
                return;
            }
            if(this.isMouseOver(mouseX, mouseY) && this.drive != null) {
                hoveringText = this.drive.entry;
            }
            this.slider.active = this.id < slots && this.drive != null;
            if(this.drive != null && this.id >= slots) {
                this.setDrive(null);
            }

            int color = 0xCF193B59;
            if(selectedSlot == this.id) {
                color = 0xFF06559C;
            }
            if(this.id >= slots) {
                color = 0xFF6A0606;
            }
            fill(stack, x, y, x+16, y+16, color);
            RenderUtils.renderBorder(stack, x, y, x+16, y+16, 1, 0xFF577694);

            if(mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                fill(stack, x, y, x+16, y+16, 0x2299bbff);
            }

            this.slider.render(stack, mouseX, mouseY, ticks);


            if(this.id >= slots && mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                hoveringText = ProjectNublar.translate("gui.machine.sequencer.locked_dna_slot", String.valueOf(SequencingSynthesizerBlockEntity.getPercentageForSlot(this.id+1)), String.valueOf(blockEntity.getDinosaurAmount()));
            }

        }

        public void setDrive(DriveEntry drive) {
            if(this.drive == drive) {
                return;
            }
            this.drive = drive;
            if(this.drive == null) {
                changeDnaSelection(this.id, "", 0);
            }
            if(this.id == 0) {
                if(drive == null) {
                    slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
                } else {
                    slots = SequencingSynthesizerBlockEntity.getSlots(DriveUtils.getAmount(blockEntity.getHandler().getStackInSlot(0), drive.getKey()) / 100F);
                }
                updateAllSlots();
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(this.active && this.visible && mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                setSelectedSlot(this.id);
                return true;
            }
            return INestedGuiEventHandler.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public final boolean isDragging() {
            return this.isDragging;
        }

        @Override
        public final void setDragging(boolean p_231037_1_) {
            this.isDragging = p_231037_1_;
        }

        @Nullable
        @Override
        public IGuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable IGuiEventListener p_231035_1_) {
            this.focused = p_231035_1_;
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return Collections.singletonList(this.slider);
        }

    }

    @RequiredArgsConstructor
    private class GeneEditEntry<O> implements GuiScrollboxEntry {
        private final GeneticEntry<?, O> entry;
        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            entry.getStorage().render(stack, this.entry.getType(), this.entry.getModifier(), x, y, width, height, renderTicks);
//            if(mouseOver) {
//                hoveringText = DriveUtils.getTranslation(this.entry.getSource().getDescriptionId(), this.entry.getVariant()).append(": " + Math.round(this.entry.getAmount() * 100) + "%");
//            }
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            return false;
        }
    }

    @RequiredArgsConstructor
    private class GeneEditTintEntry implements GuiScrollboxEntry {
        private final int tintIndex;
        private final int tint;
        private final SequencingSynthesizerBlockEntity.DnaColourStorage storage;

        private int width;
        private int height;

        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            fill(stack, x, y, x+width, y+height, 0xFF000000 | this.tint);

            this.width = width;
            this.height = height;

            int squareSize = height - 2;
            int center = x + width/2;

            int overIndex = this.mouseOverSquareIndex(mouseX - x, mouseY - y);

            int primary = this.storage.getPrimary().contains(this.tintIndex) ? 0xBB518851 : 0xBB885151;
            int secondary = this.storage.getSecondary().contains(this.tintIndex) ? 0xBB518851 : 0xBB885151;

            fill(stack, center-squareSize-2, y+1, center-1, y+1+squareSize, overIndex == 0 ? 0xBB515188 : primary);
            fill(stack, center+squareSize+2, y+1, center+1, y+1+squareSize, overIndex == 1 ? 0xBB515188 : secondary);
        }

        private int mouseOverSquareIndex(double mx, double my) {
            double squareSize = this.height - 2;
            double center = this.width/2D;
            if(mx >= center-squareSize-2 && my >= 1 && mx < center-1 && my < 1+squareSize) {
                return 0;
            } else if(mx >= center+1 && my >= 1 && mx < center+squareSize+2 && my < 1+squareSize) {
                return 1;
            }
            return -1;
        }

        private void toggle(Set<Integer> set) {
            if(set.contains(this.tintIndex)) {
                set.remove(this.tintIndex);
            } else {
                set.add(this.tintIndex);
            }
            //TODO: bruh moment sync
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            int overIndex = this.mouseOverSquareIndex(relMouseX, relMouseY);
            if(overIndex == 0) {
                this.toggle(this.storage.getPrimary());
                return true;
            } else if(overIndex == 1) {
                this.toggle(this.storage.getSecondary());
                return true;
            }
            return false;
        }
    }

    @RequiredArgsConstructor
    private class DriveEntry implements GuiScrollboxEntry {

        private final String key;
        private final DriveUtils.DriveEntry driveEntry;
        private final TranslationTextComponent entry;


        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            drawString(stack, minecraft.font, this.entry, x + 2, y + 5, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            int id = selectedSlot;
            boolean ret = GuiScrollboxEntry.super.onClicked(relMouseX, relMouseY, mouseX, mouseY);
            if(id != -1) {
                String key = this.key;
                if(scrollBox.getSelectedElement() == this) {
                    slotList.get(id).setDrive(null);
                    key = "";
                    scrollBox.setSelectedElement(null);
                    ret = false;
                }
                changeDnaSelection(id, key, 0);
                //We might only need to do `slotList[id].slider.setValueDirectly(0);`
                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
            }
            return ret;
        }

        public String getKey() {
            return this.key;
        }


    }

    private class ClampedGuiSlider extends Slider {

        private final int id;
        private final DnaSelectModuleSlot module;

        ClampedGuiSlider(int id, int x, int y, DnaSelectModuleSlot module) {
            super(x, y, 70, 16, new StringTextComponent(""), new StringTextComponent("%"), 0D, id == 0 ? 100D : 50D, DnaEditingScreen.this.blockEntity.getSelectAmount(id), false, true, p_onPress_1_ -> {});
            this.id = id;
            this.module = module;
        }

        @Override
        public void updateSlider() {
            if(module.drive != null) {
                changeDnaSelection(this.id, module.drive.getKey(), this.getSliderValue());

                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
                //Don't need to call super as it's done in `setValueDirectly`
            }
        }

        public double getSliderValue() {
            return this.sliderValue * (this.id == 0 ? 1 : 0.5D);
        }

        public void setValueDirectly(double d) {
            this.sliderValue = d / (this.id == 0 ? 1 : 0.5D);
            super.updateSlider(); //Must be super to prevent loops
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partial) {
            if(!this.visible) {
                return;
            }
            if (this.dragging) {
                this.sliderValue = (mouseX - (this.x + 4)) / (float)(this.width - 8);
                updateSlider();
            }
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int thumbLeft = this.x + (int)(this.sliderValue * (float)(this.width - 8));
            int thumbTop = this.y + 1;
            int thumbRight = thumbLeft + 8;
            int thumbBottom = thumbTop + this.height - 2;

            StencilStack.pushSquareStencil(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, StencilStack.Type.NOT);
            fill(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 0xCF193B59);
            if(this.isHovered && this.active) {
                fill(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 0x2299bbff);
            }
            RenderUtils.renderBorderExclusive(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 1, 0xFF577694);
            StencilStack.popStencil();

            fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0xCF193B59);
            if(this.isHovered && this.active) {
                fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0x2299bbff);
            }
            RenderUtils.renderBorderExclusive(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 1, 0xFF577694);

            ITextComponent buttonText = this.getMessage();
            int strWidth = mc.font.width(buttonText);
            int ellipsisWidth = mc.font.width("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
                buttonText = new StringTextComponent(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

            drawCenteredString(stack, mc.font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
        }
    }
}
