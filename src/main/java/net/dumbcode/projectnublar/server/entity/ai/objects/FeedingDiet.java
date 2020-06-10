package net.dumbcode.projectnublar.server.entity.ai.objects;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.utils.ItemStackUtils;
import net.dumbcode.projectnublar.server.entity.ai.FeedingResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeedingDiet {
    private final Map<IBlockState, FeedingResult> blocks = new HashMap<>();
    private final Map<ItemStack, FeedingResult> items = new HashMap<>();
    private final Map<Class<? extends Entity>, FeedingResult> entities = new HashMap<>();

    public Optional<FeedingResult> getResult(IBlockState state) {
        return Optional.ofNullable(this.blocks.get(state));
    }

    public Optional<FeedingResult> getResult(ItemStack item) {
        for (Map.Entry<ItemStack, FeedingResult> entry : this.items.entrySet()) {
            if(entry.getKey().isItemEqual(item) && ItemStackUtils.compareControlledNbt(entry.getKey().getTagCompound(), item.getTagCompound())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<FeedingResult> getResult(Entity entity) {
        Class clazz = entity.getClass();
        while(clazz != Entity.class) {
            if(this.entities.containsKey(clazz)) {
                return Optional.of(this.entities.get(clazz));
            }
            clazz = clazz.getSuperclass();
        }
        return Optional.empty();
    }

    public FeedingDiet add(int food, int water, IBlockState... states) {
        FeedingResult result = new FeedingResult(food, water);
        for (IBlockState state : states) {
            this.blocks.put(state, result);
        }
        return this;
    }

    public FeedingDiet add(int food, int water, Block... blocks) {
        for (Block block : blocks) {
            this.add(food, water, block.getBlockState().getValidStates().toArray(new IBlockState[0]));
        }
        return this;
    }

    public final FeedingDiet add(int food, int water, ItemStack... stackPredicates) {
        FeedingResult result = new FeedingResult(food, water);
        for (ItemStack stack : stackPredicates) {
            this.items.put(stack, result);
        }
        return this;
    }

    public FeedingDiet add(int food, int water, Item... items) {
        FeedingResult result = new FeedingResult(food, water);
        for (Item item : items) {
            this.items.put(new ItemStack(item), result);
        }
        return this;
    }

    @SafeVarargs
    public final FeedingDiet add(int food, int water, Class<? extends Entity>... entities) {
        FeedingResult result = new FeedingResult(food, water);
        for (Class<? extends Entity> aClass : entities) {
            this.entities.put(aClass, result);
        }
        return this;
    }

    public FeedingDiet copyInto(FeedingDiet diet) {
        diet.blocks.clear();
        diet.blocks.putAll(this.blocks);

        diet.items.clear();
        diet.items.putAll(this.items);

        diet.entities.clear();
        diet.entities.putAll(this.entities);

        return diet;
    }


    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList blocks = new NBTTagList();
        NBTTagList items = new NBTTagList();
        NBTTagList entities = new NBTTagList();

        this.blocks.forEach((state, result) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("blockstate", NBTUtil.writeBlockState(new NBTTagCompound(), state));
            compound.setTag("result", FeedingResult.writeToNBT(new NBTTagCompound(), result));
            blocks.appendTag(compound);
        });
        this.items.forEach((stack, result) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("itemstack", stack.serializeNBT());
            compound.setTag("result", FeedingResult.writeToNBT(new NBTTagCompound(), result));
            items.appendTag(compound);
        });
        this.entities.forEach((entityClass, result) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("classname", new NBTTagString(entityClass.getName()));
            compound.setTag("result", FeedingResult.writeToNBT(new NBTTagCompound(), result));
            entities.appendTag(compound);
        });

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
            NBTTagCompound compound = (NBTTagCompound) base;
            this.blocks.put(NBTUtil.readBlockState(compound.getCompoundTag("blockstate")), FeedingResult.readFromNbt(compound.getCompoundTag("result")));
        }
        for (NBTBase base : nbt.getTagList("items", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound compound = (NBTTagCompound) base;
            this.items.put(new ItemStack(compound.getCompoundTag("itemstack")), FeedingResult.readFromNbt(compound.getCompoundTag("result")));
        }
        for (NBTBase base : nbt.getTagList("entities", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound compound = (NBTTagCompound) base;
            try {
                Class<?> clazz = Class.forName(compound.getString("classname"));
                if(Entity.class.isAssignableFrom(clazz)) {
                    this.entities.put((Class<? extends Entity>) clazz, FeedingResult.readFromNbt(compound.getCompoundTag("result")));
                } else {
                    DumbLibrary.getLogger().warn("Skipping {} as it does not extend Entity", clazz);
                }
            } catch (ClassNotFoundException e) {
                DumbLibrary.getLogger().warn("Skipping class {} as it does not exist", ((NBTTagString) base).getString());
            }
        }
    }
}
