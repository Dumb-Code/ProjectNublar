package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.ItemStackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.List;

@Getter
public class FeedingDiet {
    private final List<IBlockState> blocks = Lists.newArrayList();
    private final List<ItemStack> items = Lists.newArrayList();
    private final List<Class<? extends Entity>> entities = Lists.newArrayList();

    public boolean test(IBlockState state) {
        return this.blocks.contains(state);
    }

    public boolean test(ItemStack item) {
        for (ItemStack stack : this.items) {
            if(stack.isItemEqual(item) && ItemStackUtils.compareControlledNbt(stack.getTagCompound(), item.getTagCompound())) {
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

    public final FeedingDiet add(ItemStack... stackPredicates) {
        this.items.addAll(Arrays.asList(stackPredicates));
        return this;
    }

    public FeedingDiet add(Item... items) {
        for (Item item : items) {
            this.items.add(new ItemStack(item));
        }
        return this;
    }

    @SafeVarargs
    public final FeedingDiet add(Class<? extends Entity>... entities) {
        this.entities.addAll(Arrays.asList(entities));
        return this;
    }


    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList blocks = new NBTTagList();
        NBTTagList items = new NBTTagList();
        NBTTagList entities = new NBTTagList();

        for (IBlockState block : this.blocks) {
            blocks.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), block));
        }
        for (ItemStack item : this.items) {
            items.appendTag(item.serializeNBT());
        }
        for (Class<? extends Entity> entity : this.entities) {
            entities.appendTag(new NBTTagString(entity.getName()));
        }

        nbt.setTag("blocks", blocks);
        nbt.setTag("items", items);
        nbt.setTag("entities", entities);
        return nbt;
    }

    @SuppressWarnings("unchecked")
    public void fromNBT(NBTTagCompound nbt) {
        this.blocks.clear();
        this.items.clear();
        this.entities.clear();

        for (NBTBase base : nbt.getTagList("blocks", Constants.NBT.TAG_COMPOUND)) {
            this.blocks.add(NBTUtil.readBlockState((NBTTagCompound)base));
        }
        for (NBTBase base : nbt.getTagList("items", Constants.NBT.TAG_COMPOUND)) {
            this.items.add(new ItemStack((NBTTagCompound) base));
        }
        for (NBTBase base : nbt.getTagList("entities", Constants.NBT.TAG_STRING)) {
            try {
                Class<?> clazz = Class.forName(((NBTTagString) base).getString());
                if(Entity.class.isAssignableFrom(clazz)) {
                    this.entities.add((Class<? extends Entity>) clazz);
                } else {
                    ProjectNublar.getLogger().warn("Skipping {} as it does not extend Entity", clazz);
                }
            } catch (ClassNotFoundException e) {
                ProjectNublar.getLogger().warn("Skipping class {} as it does not exist", ((NBTTagString) base).getString());
            }
        }

    }


}
