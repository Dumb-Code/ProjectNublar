package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.gui.SimpleButton;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.dna.GeneticHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C2SSequencingSynthesizerIsolationChange;
import net.dumbcode.projectnublar.server.network.C2SSequencingSynthesizerIsolationRemoved;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AdvancedDnaEditingScreen extends DnaEditingScreen {

    private static final int CHROMO_TABLE_WIDTH = 4; //cells
    private static final int CHROMO_TABLE_HEIGHT = 20; //cells

    private static final int CHROMO_CELL_WIDTH = 16; //px
    private static final int CHROMO_CELL_HEIGHT = 5; //px

    private static final int CHROMO_CELL_PADDING = 2;

    private static final int CHROMO_FULL_CELL_WIDTH = CHROMO_CELL_WIDTH + CHROMO_CELL_PADDING;
    private static final int CHROMO_FULL_CELL_HEIGHT = CHROMO_CELL_HEIGHT + CHROMO_CELL_PADDING;

    private static final int TOTAL_WIDTH = CHROMO_TABLE_WIDTH* CHROMO_FULL_CELL_WIDTH - CHROMO_CELL_PADDING;
    private static final int TOTAL_HEIGHT = CHROMO_TABLE_HEIGHT* CHROMO_FULL_CELL_HEIGHT - CHROMO_CELL_PADDING;

    //minCol = 0x27557A;
    //maxCol = 0x4D99B1;
    private static final int CHROMOSOME_COLOUR_MIN_R = 0x27;
    private static final int CHROMOSOME_COLOUR_R_RANGE = 0x4D - 0x27;
    private static final int CHROMOSOME_COLOUR_MIN_G = 0x55;
    private static final int CHROMOSOME_COLOUR_G_RANGE = 0x99 - 0x55;
    private static final int CHROMOSOME_COLOUR_MIN_B = 0x7A;
    private static final int CHROMOSOME_COLOUR_B_RANGE = 0xB1 - 0x7A;

    private final Chromosome[] chromosomes = new Chromosome[CHROMO_TABLE_WIDTH * CHROMO_TABLE_HEIGHT];

    private int chromoTableLeft;
    private int chromoTableTop;
    private int previousHash;
    private int selectedId = -1;
    private int hoveredId = -1;

    private Widget elementWidget;

    private Button resetButton;


    public AdvancedDnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(blockEntity, inventorySlotsIn, playerInventory, title, bar, "basic", 1);
    }

    @Override
    public void init() {
        super.init();
        this.chromoTableLeft = this.leftPos + 20;//this.leftPos + (this.imageWidth - TOTAL_WIDTH) / 2;
        this.chromoTableTop = 1 + this.topPos + (this.imageHeight - TOTAL_HEIGHT) / 2;

        this.resetButton = this.addButton(new SimpleButton(this.leftPos + 232, this.topPos + 107, 109, 16, ProjectNublar.translate("gui.machine.sequencer.reset"), b -> {
            if(this.blockEntity.isProcessingMain()) {
                return;
            }
            if(this.selectedId != -1) {
                Chromosome chromosome = this.chromosomes[this.selectedId];
                chromosome.resetButton();

                this.children.remove(this.elementWidget);
                this.buttons.remove(this.elementWidget);
                this.elementWidget = chromosome.createWidget(this.leftPos + 232, this.topPos + 23, 109, 80);
                this.children.add(this.elementWidget);
                this.buttons.add(this.elementWidget);
            }
        }));
        this.resetButton.active = this.selectedId != -1;
        this.onSelectChange();
    }

    @Override
    public void onSlotChanged(int slot, ItemStack stack) {
        super.onSlotChanged(slot, stack);
        if(slot == 0) {
            this.generateChromosomes(stack);
        }
    }

    @Override
    public void onSelectChange() {
        super.onSelectChange();
        this.generateChromosomes(this.blockEntity.getHandler().getStackInSlot(0));
    }

    private void generateChromosomes(ItemStack drive) {
        int hashGenetics = this.hashGenetics();
        Random random = new Random(hashGenetics);
        random.nextBytes(new byte[256]); //Just so if we get a bad seed we don't have the same results.

        List<Chromosome> list = new ArrayList<>();
        List<DriveUtils.IsolatedGeneEntry> isolatedGenes = DriveUtils.getAllIsolatedGenes(drive);
        for (int i = 0; i < this.chromosomes.length; i++) {
            if(i == 0) {
                //If more than 80% of the dinosaur is sequenced, add gender
                String dinosaurKey = this.blockEntity.getSelectKey(0);
                int amount = DriveUtils.getAmount(drive, dinosaurKey, null);
                if (amount > 80) {
                    list.add(new GenderChromosome(this.blockEntity, random.nextFloat()));
                    continue;
                }
            }


            boolean full = !isolatedGenes.isEmpty() && isolatedGenes.get(0).getProgress() == 1;
            //If is the tint, add both the primary and secondary.
            if(full && isolatedGenes.get(0).getGeneticType() == GeneticTypes.OVERALL_TINT.get()) {
                list.add(new GeneticTintChromosome(this.blockEntity, false, random.nextFloat()));
                list.add(new GeneticTintChromosome(this.blockEntity, true, random.nextFloat()));
                i++;
            } else if(full) {
                list.add(new GeneticChromosome(this.blockEntity, isolatedGenes.get(0).getGeneticType(), random.nextFloat()));
            } else {
                list.add(new EmptyChromosome(random.nextFloat()));
            }
            if(!isolatedGenes.isEmpty()) {
                isolatedGenes.remove(0);
            }
        }

        Collections.shuffle(list, random);
        for (int i = 0; i < this.chromosomes.length; i++) {
            this.chromosomes[i] = list.get(i);
        }

        if(hashGenetics != this.previousHash) {
            this.previousHash = hashGenetics;
            this.deselect();
        }

    }

    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        this.hoveredId = -1;
        for (int x = 0; x < CHROMO_TABLE_WIDTH; x++) {
            int xStart = this.chromoTableLeft + x*CHROMO_FULL_CELL_WIDTH;
            for (int y = 0; y < CHROMO_TABLE_HEIGHT; y++) {
                int yStart = this.chromoTableTop + y*CHROMO_FULL_CELL_HEIGHT;

                int id = x + y * CHROMO_TABLE_WIDTH;
                Chromosome chromosome = this.chromosomes[id];

                int colour;
                if(!chromosome.isEmpty()) {
                    boolean selected = this.selectedId == id;

                    colour = selected ? 0xFFD93434 : 0xFFB340B1;
                    if(mouseX >= xStart && mouseX < xStart + CHROMO_CELL_WIDTH && mouseY >= yStart && mouseY < yStart + CHROMO_CELL_HEIGHT) {
                        this.hoveringText = chromosome.getText().orElse(this.hoveringText);
                        this.hoveredId = id;
                        colour = selected ? 0xFFD16969 : 0xFFB55EB4;
                    }


                } else {
                    int colourR = (int) (CHROMOSOME_COLOUR_MIN_R + CHROMOSOME_COLOUR_R_RANGE * chromosome.getRandomDist());
                    int colourG = (int) (CHROMOSOME_COLOUR_MIN_G + CHROMOSOME_COLOUR_G_RANGE * chromosome.getRandomDist());
                    int colourB = (int) (CHROMOSOME_COLOUR_MIN_B + CHROMOSOME_COLOUR_B_RANGE * chromosome.getRandomDist());

                    colour = 0xFF000000 | (colourR << 16) | (colourG << 8) | colourB;
                }
                fill(stack, xStart, yStart, xStart + CHROMO_CELL_WIDTH, yStart + CHROMO_CELL_HEIGHT, colour);
            }
        }
    }

    private void deselect() {
        this.selectedId = -1;
        this.resetButton.active = false;
        this.children.remove(this.elementWidget);
        this.buttons.remove(this.elementWidget);
        this.elementWidget = null;
    }

    public void selectHovered() {
        this.selectedId = this.hoveredId;
        this.resetButton.active = true;
        Chromosome chromosome = this.chromosomes[this.selectedId];
        this.elementWidget = chromosome.createWidget(this.leftPos + 232, this.topPos + 23, 109, 80);
        this.children.add(this.elementWidget);
        this.buttons.add(this.elementWidget);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(!this.blockEntity.isProcessingMain() && this.hoveredId != -1) {
            //If already selected, toggle
            boolean set = this.selectedId != this.hoveredId;
            this.deselect();
            if(set) {
                this.selectHovered();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private int hashGenetics() {
        int result = 1;
        for (int i = 0; i < SequencingSynthesizerBlockEntity.TOTAL_SLOTS; i++) {
            result = result * 31 + this.blockEntity.getSelectKey(i).hashCode();
            result = result * 31 + Double.hashCode(this.blockEntity.getSelectAmount(i));
        }
        return result;
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        boolean normalRet = this.getFocused() != null && this.isDragging() && p_231045_5_ == 0 && this.getFocused().mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        return normalRet || super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
    }

    @Override
    protected int getEntityLeft() {
        return 105;
    }

    @Override
    protected int getEntityRight() {
        return 223;
    }

    @Override
    protected GuiScrollBox<GuiScrollboxEntry> createOverviewScrollBox() {
        return new GuiScrollBox<>(this.leftPos + 104, this.topPos + 134, 237, 14, 3, this::createOverviewScrollBoxList);
    }

    interface Chromosome {
        void resetButton();
        Widget createWidget(int x, int y, int width, int height);
        Optional<IFormattableTextComponent> getText();
        default boolean isEmpty() {
            return false;
        }
        float getRandomDist();
    }

    @Value
    private static class EmptyChromosome implements Chromosome {
        float randomDist; //[0-1]

        @Override
        public void resetButton() {

        }

        @Override
        public Widget createWidget(int x, int y, int width, int height) {
            return null;
        }

        @Override
        public Optional<IFormattableTextComponent> getText() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    @Value
    private static class GenderChromosome implements Chromosome {
        SequencingSynthesizerBlockEntity blockEntity;
        float randomDist; //[0-1]

        @Override
        public void resetButton() {
            this.blockEntity.setDinosaurGender(SequencingSynthesizerBlockEntity.DinosaurSetGender.RANDOM);
        }

        @Override
        public Widget createWidget(int x, int y, int width, int height) {
            int[] index = new int[]{ this.blockEntity.getDinosaurGender().ordinal() };
            SequencingSynthesizerBlockEntity.DinosaurSetGender[] values = SequencingSynthesizerBlockEntity.DinosaurSetGender.values();
            return new SimpleButton(x, y + height/2 - 10, width, 20, ProjectNublar.translate("gender.title", values[index[0]].getText()),
                press -> {
                    index[0]++;
                    SequencingSynthesizerBlockEntity.DinosaurSetGender gender = values[index[0] % values.length];
                    this.blockEntity.setDinosaurGender(gender);
                    press.setMessage(ProjectNublar.translate("gender.title", values[index[0] % values.length].getText()));
                }
            );
        }

        @Override
        public Optional<IFormattableTextComponent> getText() {
            return Optional.of(ProjectNublar.translate("genetic_type.dummy.gender"));
        }
    }


    @Value
    private static class GeneticTintChromosome implements Chromosome {
        SequencingSynthesizerBlockEntity blockEntity;
        boolean isSecondaryColour;
        float randomDist; //[0-1]

        @Override
        public void resetButton() {
            @SuppressWarnings("unchecked")
            SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<GeneticTint> entry = (SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<GeneticTint>) blockEntity.getIsolationOverrides().get(GeneticTypes.OVERALL_TINT.get());
            if(entry != null) {
                GeneticTint value = entry.getValue();
                GeneticTint newTint = new GeneticTint(
                    this.isSecondaryColour ? value.getPrimary() : new GeneticTint.Part(1, 1, 1, 1, 0),
                    this.isSecondaryColour ? new GeneticTint.Part(1, 1, 1, 1, 0) : value.getSecondary()
                );
                entry.setValue(newTint);
                ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerIsolationChange(entry));
            }
        }

        @Override
        public Widget createWidget(int x, int y, int width, int height) {
            return createWidgetFromEntry(this.blockEntity.getOrCreateIsolationEntry(GeneticTypes.OVERALL_TINT.get()), x, y, width, height, 0);
        }

        @Override
        public Optional<IFormattableTextComponent> getText() {
            return Optional.of(ProjectNublar.translate("genetic_type.dummy.colour." + (this.isSecondaryColour ? "secondary" : "primary")));
        }
    }


    @Value
    private static class GeneticChromosome implements Chromosome {
        SequencingSynthesizerBlockEntity blockEntity;
        @Nullable GeneticType<?, ?> directEditType;
        float randomDist; //[0-1]

        @Override
        public void resetButton() {
            blockEntity.removeIsolationEntry(this.directEditType);
            ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerIsolationRemoved(this.directEditType));
        }

        @Override
        public Widget createWidget(int x, int y, int width, int height) {
            return createWidgetFromEntry(this.blockEntity.getOrCreateIsolationEntry(this.directEditType), x, y, width, height, 0);
        }

        @Override
        public Optional<IFormattableTextComponent> getText() {
            if(this.directEditType != null) {
                return Optional.of(this.directEditType.getTranslationComponent());
            }
            return Optional.empty();
        }
    }

    private static <O> Widget createWidgetFromEntry(SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<O> entry, int x, int y, int width, int height, int data) {
        return entry.getType().getDataHandler().createIsolationWidget(x, y, width, height, data, entry::getValue, o -> {
            entry.setValue(o);
            ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerIsolationChange(entry));
        }, entry.getType());
    }
}
