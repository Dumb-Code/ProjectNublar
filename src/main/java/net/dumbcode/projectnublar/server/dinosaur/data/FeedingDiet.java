package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class FeedingDiet {
    private final List<IBlockState> blocks = Lists.newArrayList();
    private final List<Predicate<ItemStack>> items = Lists.newArrayList();
    private final List<Class<? extends Entity>> entities = Lists.newArrayList();

    public boolean test(IBlockState state) {
        return this.blocks.contains(state);
    }

    public boolean test(ItemStack item) {
        for (Predicate<ItemStack> predicate : this.items) {
            if(predicate.test(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean test(Entity entity) {
        Class clazz = entity.getClass();
        while(clazz != Entity.class) {
            if(this.entities.contains(clazz)) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public FeedingDiet add(IBlockState... state) {
        this.blocks.addAll(Arrays.asList(state));
        return this;
    }

    public FeedingDiet add(Block... blocks) {
        for (Block block : blocks) {
            this.blocks.addAll(block.getBlockState().getValidStates());
        }
        return this;
    }

    @SafeVarargs
    public final FeedingDiet add(Predicate<ItemStack>... stackPredicates) {
        this.items.addAll(Arrays.asList(stackPredicates));
        return this;
    }

    public FeedingDiet add(Item... items) {
        for (Item item : items) {
            this.items.add(i -> i.getItem() == item);
        }
        return this;
    }

    @SafeVarargs
    public final FeedingDiet add(Class<? extends Entity>... entities) {
        this.entities.addAll(Arrays.asList(entities));
        return this;
    }


}
