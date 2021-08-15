package net.dumbcode.projectnublar.server.dna.storages;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class GeneticDietChangeStorage extends RandomUUIDStorage<Float> {

    @Getter
    @Setter
    @Accessors(chain = true)
    private FeedingDiet diet = new FeedingDiet();

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        this.diet.writeToNBT(nbt);
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        super.deserialize(nbt);
        this.diet.fromNBT(nbt);
    }

    @Override
    public void render(MatrixStack stack, GeneticType<?, Float> entry, Float value, int x, int y, int width, int height, float ticks) {
        TranslationTextComponent component = new TranslationTextComponent(DumbLibrary.MODID + ".genetic_storage.diet");
        FontRenderer font = Minecraft.getInstance().font;

        int w = font.width(component);
        font.draw(stack, component, x+2, y+4, -1);

        List<ITextComponent> allComponents = new ArrayList<>();
        for (BlockState block : this.diet.getBlocks()) {
            allComponents.add(block.getBlock().getName());
        }
        for (ItemStack item : this.diet.getItems()) {
            allComponents.add(item.getHoverName());
        }
        for (EntityType<?> entity : this.diet.getEntities()) {
            allComponents.add(entity.getDescription());
        }

        IFormattableTextComponent joined = new StringTextComponent("");
        int size = allComponents.size();
        for (int i = 0; i < size; i++) {
            joined.append(allComponents.get(i));
            if(i != size-1) {
                joined.append(", ");
            }
        }

        RenderUtils.renderScrollingText(stack, joined, ticks, x+w+2, y+4, width-w-4, 0xFFAAAAAA);
    }


    @Override
    public Object getCombinerKey() {
        return this.diet;
    }
}
