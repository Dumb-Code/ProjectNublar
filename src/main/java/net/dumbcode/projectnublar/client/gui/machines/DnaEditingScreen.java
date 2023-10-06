package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import com.mojang.math.Axis;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
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
import net.dumbcode.projectnublar.server.network.C2SChangeContainerTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.List;

public abstract class DnaEditingScreen extends SequencerSynthesizerBaseScreen {

    private static final ResourceLocation BASE_LOCATION = get("editing_page");

    private DinosaurEntity cachedEntity;

    private final List<GeneticEntry<?, ?>> combineGenetics = Lists.newArrayList();
    private final List<GuiScrollboxEntry> combinedEditGeneList = Lists.newArrayList();

    protected final SequencingSynthesizerBlockEntity blockEntity;
    private GuiScrollBox<GuiScrollboxEntry> geneScrollBox;

    private float renderTicks;

    private final Component nextTab;
    private final int nextTabIndex;

    protected Component hoveringText;

    public DnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar, String nextTabName, int nextTabIndex) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;

        this.nextTab = ProjectNublar.translate("gui.machine.sequencer.dna_editing.tab." + nextTabName);
        this.nextTabIndex = nextTabIndex;
    }

    @Override
    public void init() {
        super.init();
        this.geneScrollBox = this.addWidget(this.createOverviewScrollBox())
            .addFromPrevious(this.geneScrollBox)
            .setCellsPerRow(2)
            .setBorderColor(0xFF577694)
            .setCellHighlightColor(0xCF193B59)
            .setCellSelectedColor(0xFF06559C)
            .setInsideColor(0xFF193B59)
            .setEmptyColor(0xCF0F2234)
            .setRenderFullSize(true);

    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        hoveringText = null;
        super.render(stack, mouseX, mouseY, ticks);
        if(hoveringText != null) {
            int width = minecraft.font.width(hoveringText);
            stack.fill(mouseX+5, mouseY-1, mouseX + width + 6, mouseY + minecraft.font.lineHeight+1, 0xFF23374A);
            RenderUtils.renderBorderExclusive(stack, mouseX+5, mouseY-1, mouseX + width +6, mouseY + minecraft.font.lineHeight+1, 1, 0xFF577694);
            stack.drawString(minecraft.font, hoveringText, mouseX + 6, mouseY, -1);
        }
    }


    @Override
    protected void renderBg(GuiGraphics stack, float ticks, int mouseX, int mouseY) {
        stack.blit(BASE_LOCATION, this.leftPos, this.topPos, (int) stack.pose().last().pose().getTranslation(new Vector3f()).z, 0, 0, this.imageWidth, this.imageHeight, this.imageHeight, this.imageWidth); //There is a vanilla bug that mixes up width and height
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        super.renderLabels(stack, mouseX, mouseY);

        int width = font.width(this.nextTab);
        stack.drawString(font, this.nextTab.getString(), 48 - width/2F, 10, -1, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        double mx = mouseX - this.leftPos;
        double my = mouseY - this.topPos;

        if(mx >= 14 && mx < 83 && my >= 8 && my < 18) {
            ProjectNublar.NETWORK.sendToServer(new C2SChangeContainerTab(this.nextTabIndex, this.sequencerBlockPos));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void containerTick() {
        if(this.cachedEntity != null) {
            this.cachedEntity.get(EntityComponentTypes.GENDER.get()).ifPresent(g -> {
                SequencingSynthesizerBlockEntity.DinosaurSetGender gender = this.blockEntity.getDinosaurGender();
                if(gender.hasValue()) {
                    g.male = gender.getMale();
                } else if(Minecraft.getInstance().player.tickCount % 20 == 0) {
                    g.male = !g.male;
                }
            });
        }
    }

    protected abstract int getEntityLeft();
    protected abstract int getEntityRight();

    @Override
    public void renderScreen(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        super.renderScreen(stack, mouseX, mouseY, ticks);
        if(this.cachedEntity != null) {
            AABB a = this.cachedEntity.getBoundingBoxForCulling();
            double min = Math.max(a.getXsize(), Math.max(a.getYsize(), a.getZsize()));

            int left = this.getEntityLeft();
            int right = this.getEntityRight();
            int middle = (left + right) / 2;

            stack.fill(this.leftPos+left, this.topPos+24, this.leftPos+right, this.topPos+123, 0xAF193B59);
            RenderUtils.renderBorderExclusive(stack, this.leftPos+left, this.topPos+24, this.leftPos+right, this.topPos+123, 1, 0xFF577694);

            StencilStack.pushSquareStencil(stack, this.leftPos+left, this.topPos+24, this.leftPos+right, this.topPos+123);
            renderEntityAndPlayer(this.leftPos+middle, this.topPos + 110, 200 / min, this.cachedEntity, mouseX, mouseY);
            StencilStack.popStencil();
        }
        renderTicks += ticks;
    }

    protected abstract GuiScrollBox<GuiScrollboxEntry> createOverviewScrollBox();

    protected List<GuiScrollboxEntry> createOverviewScrollBoxList() {
        return this.combinedEditGeneList;
    }

    public void onSelectChange() {
        this.onGeneticsChanged();
    }

    public void onGeneticsChanged() {
        this.combinedEditGeneList.clear();
        this.combineGenetics.clear();
        this.combineGenetics.addAll(this.blockEntity.gatherAllGeneticEntries());
        for (GeneticEntry<?, ?> entry : this.combineGenetics) {
            this.combinedEditGeneList.add(new GeneEditEntry<>(entry));
        }

        this.refreshEntity();
    }

    protected void refreshEntity() {
        String key = this.blockEntity.getSelectKey(0);
        double amount = this.blockEntity.getSelectAmount(0);
        boolean hasDinosaur = !key.isEmpty() && amount != 0;
        if(hasDinosaur) {
            Dinosaur dinosaur = DinosaurHandler.getRegistry().getValue(new ResourceLocation(key));
            if(this.cachedEntity == null || this.cachedEntity.getDinosaur() != dinosaur) {
                this.cachedEntity = dinosaur.createEntity(
                    Minecraft.getInstance().level,
                    dinosaur.getAttacher().getDefaultConfig()
                        .runBeforeFinalize(EntityComponentTypes.GENETICS.get(), GeneticComponent::disableRandomGenetics)
                );
            }
            this.cachedEntity.get(EntityComponentTypes.GENETICS).ifPresent(g -> GeneticComponent.replaceGenetics(g, this.cachedEntity, this.combineGenetics));
        } else if(this.cachedEntity != null) {
            this.cachedEntity = null;
        }
    }

    private static void renderEntityAndPlayer(int x, int y, double scale, Entity entity, double mouseX, double mouseY) {
        renderEntityAt(x, y, scale, entity);

        LocalPlayer player = Minecraft.getInstance().player;
        double mx = x + 20 - mouseX;
        double my = y - scale*1.5 - mouseY;

        float angleX = (float)Math.atan(mx / 40.0F);
        float angleY = (float)Math.atan(my / 40.0F);

        float yBodyRot = player.yBodyRot;
        float yRot = player.yRotO;
        float xRot = player.xRotO;
        float yHeadRotO = player.yHeadRotO;
        float yHeadRot = player.yHeadRot;

        player.yBodyRot = 180.0F + angleX * 20.0F;
        player.yRotO = 180.0F + angleX * 40.0F;
        player.xRotO = -angleY * 20.0F;
        player.yHeadRot = player.yRotO;
        player.yHeadRotO = player.yRotO;

        renderEntityAt(x + 20, y, scale, player, 900, Axis.XP.rotationDegrees(angleY * 20.0F));

        player.yBodyRot = yBodyRot;
        player.yRotO = yRot;
        player.xRotO = xRot;
        player.yHeadRotO = yHeadRotO;
        player.yHeadRot = yHeadRot;
    }

    @RequiredArgsConstructor
    public class GeneEditEntry<O> implements GuiScrollboxEntry {
        private final GeneticEntry<?, O> entry;
        @Override
        public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
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
}
