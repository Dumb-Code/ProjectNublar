package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Value;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.*;

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
    @Nullable
    private GeneticType<?, ?> selectedType;
    @Nullable
    private GeneticType<?, ?> hoveredGeneticType;


    public AdvancedDnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(blockEntity, inventorySlotsIn, playerInventory, title, bar, "basic", 1);
        this.generateChromosomes(blockEntity.getHandler().getStackInSlot(0));
    }

    @Override
    public void init() {
        super.init();
        this.chromoTableLeft = this.leftPos + 20;//this.leftPos + (this.imageWidth - TOTAL_WIDTH) / 2;
        this.chromoTableTop = 1 + this.topPos + (this.imageHeight - TOTAL_HEIGHT) / 2;
    }

    @Override
    public void onSlotChanged(int slot, ItemStack stack) {
        super.onSlotChanged(slot, stack);
        if(slot == 0) {
            this.generateChromosomes(stack);
        }
    }

    private void generateChromosomes(ItemStack drive) {
        Random random = new Random(this.hashGenetics());
        random.nextBytes(new byte[256]); //Just so if we get a bad seed we don't have the same results.

        List<Chromosome> list = new ArrayList<>();
        List<DriveUtils.IsolatedGeneEntry> isolatedGenes = DriveUtils.getAllIsolatedGenes(drive);
        for (int i = 0; i < this.chromosomes.length; i++) {
            list.add(new Chromosome(isolatedGenes.isEmpty() ? null : isolatedGenes.remove(0).getGeneticType(), random.nextFloat()));
        }
        Collections.shuffle(list, random);
        for (int i = 0; i < this.chromosomes.length; i++) {
            this.chromosomes[i] = list.get(i);
        }
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        super.renderScreen(stack, mouseX, mouseY, ticks);
    }

    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        this.hoveredGeneticType = null;
        for (int x = 0; x < CHROMO_TABLE_WIDTH; x++) {
            int xStart = this.chromoTableLeft + x*CHROMO_FULL_CELL_WIDTH;
            for (int y = 0; y < CHROMO_TABLE_HEIGHT; y++) {
                int yStart = this.chromoTableTop + y*CHROMO_FULL_CELL_HEIGHT;

                Chromosome chromosome = this.chromosomes[x+y*CHROMO_TABLE_WIDTH];

                int colour;
                if(chromosome.getDirectEditType() != null) {
                    boolean selected = this.selectedType == chromosome.getDirectEditType();

                    colour = selected ? 0xFFD93434 : 0xFFB340B1;
                    if(mouseX >= xStart && mouseX < xStart + CHROMO_CELL_WIDTH && mouseY >= yStart && mouseY < yStart + CHROMO_CELL_HEIGHT) {
                        this.hoveringText = chromosome.getDirectEditType().getTranslationComponent();
                        this.hoveredGeneticType = chromosome.getDirectEditType();
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
//        fill(stack, this.chromoTableLeft, this.chromoTableTop, 200, 200, -1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(this.hoveredGeneticType != null) {
            //If already selected, toggle
            if(this.selectedType == this.hoveredGeneticType) {
                this.selectedType = null;
            } else {
                this.selectedType = this.hoveredGeneticType;
            }
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
    protected int getEntityLeft() {
        return 222;
    }

    @Override
    protected int getEntityRight() {
        return 340;
    }

    @Override
    protected GuiScrollBox<GuiScrollboxEntry> createOverviewScrollBox() {
        return new GuiScrollBox<>(this.leftPos + 222, this.topPos + 134, 118, 14, 3, this::createOverviewScrollBoxList);
    }

    @Value
    private static class Chromosome {
        @Nullable GeneticType<?, ?> directEditType;
        float randomDist; //[0-1]
    }
}
