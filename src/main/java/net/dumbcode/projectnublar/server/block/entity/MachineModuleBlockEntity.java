package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class MachineModuleBlockEntity<B extends MachineModuleBlockEntity<B>> extends SimpleBlockEntity implements ITickable {

    public final Collection<MachineRecipe<B>> recipes = Collections.unmodifiableCollection(this.getAllRecipes());
    private final B asB = asB();

    @Getter private final MachineModuleItemStackHandler handler = new MachineModuleItemStackHandler<>(this, 9);
    protected final List<Process> processes = Lists.newArrayList();

    @Getter @Setter private int stateID;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("StateID", this.stateID);
        compound.setInteger("ProcessCount", this.processes.size());
        for (int i = 0; i < this.processes.size(); i++) {
            Process process = this.processes.get(i);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("Time", process.time);
            nbt.setInteger("TotalTime", process.totalTime);
            if(process.currentRecipe != null) {
                nbt.setString("Recipe", process.currentRecipe.toString());
            }
            nbt.setBoolean("Processing", process.processing); //Is this really needed?
            compound.setTag("Process_" + i, nbt);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stateID = compound.getInteger("StateID");
        for (int i = 0; i < compound.getInteger("ProcessCount"); i++) {
            Process process = this.processes.get(i);
            NBTTagCompound nbt = compound.getCompoundTag("Process_" + i);
            process.setTime(nbt.getInteger("Time"));
            process.setTotalTime(nbt.getInteger("TotalTime"));
            process.setCurrentRecipe(nbt.hasKey("Recipe", Constants.NBT.TAG_STRING) ? new ResourceLocation(nbt.getString("Recipe")) : null);
            process.setProcessing(nbt.getBoolean("Processing")); //Is this really needed?
        }
    }

    @Override
    public void update() {
        for (Process process : this.processes) {
            if(this.canProcess(process)) {
                if(process.isProcessing() || this.searchForRecipes(process)) {
                    if(process.isFinished()) {
                        MachineRecipe<B> recipe = this.getRecipe(process);
                        if(recipe != null) {
                            recipe.onRecipeFinished(this.asB, process);
                            process.setTime(0);
                            if(!recipe.accpets(this.asB, process)) {
                                process.setProcessing(false);
                                process.setCurrentRecipe(null);
                                this.searchForRecipes(process);
                            }
                        } else {
                            ProjectNublar.getLogger().error("Unable to find recipe " + process.getCurrentRecipe() + " as it does not exist.");
                        }
                    } else {
                        process.tick();
                    }
                    this.markDirty();
                }
            } else if(process.isProcessing()) {
                this.getInterruptAction(process).processConsumer.accept(process);
            }
        }

        this.processes.removeIf(Process::isFinished);
    }

    @Nullable
    public Process getProcess(int slot) {
        for (Process process : this.processes) {
            for (int i : process.getInputSlots()) {
                if(i == slot) {
                    return process;
                }
            }
        }
        return null;
    }

    public boolean searchForRecipes(Process process) {
        for (MachineRecipe<B> recipe : this.recipes) {
            if(recipe.accpets(this.asB, process) && this.canProcess(process)) {
                process.setProcessing(true);
                process.setCurrentRecipe(recipe.getRegistryName());
                process.setTotalTime(recipe.getRecipeTime(this.asB, process));
                this.markDirty();
                return true;
            }
        }
        return false;
    }

    @Nullable
    public MachineRecipe<B> getRecipe(Process process) {
        if(process.getCurrentRecipe() != null) {
            for (MachineRecipe<B> recipe : this.recipes) {
                if(recipe.getRegistryName().equals(process.getCurrentRecipe())) {
                    return recipe;
                }
            }
        }
        return null;
    }

    protected boolean canProcess(Process process) {
        return true;
    }

    protected ProcessInterruptAction getInterruptAction(Process process) {
        return ProcessInterruptAction.RESET;
    }

    protected abstract int getInventorySize();

    public abstract boolean isItemValidFor(int slot, ItemStack stack);

    protected abstract List<MachineRecipe<B>> getAllRecipes();

    protected abstract B asB();

    @Getter
    @Setter
    public static class Process {
        final int[] inputSlots;
        final int[] outputSlots;

        int time;
        int totalTime;
        ResourceLocation currentRecipe;
        boolean processing;

        public Process(int[] inputSlots, int[] outputSlots) {
            this.inputSlots = inputSlots;
            this.outputSlots = outputSlots;
        }

        public void tick() {
            this.time++;
        }

        public boolean isFinished() {
            return this.time >= this.totalTime;
        }
    }

    protected enum ProcessInterruptAction {
        RESET(p -> p.setTime(0)),
        DECREASE(p -> p.setTime(p.getTime() - 1)),
        PAUSE(p -> {});

        private final Consumer<Process> processConsumer;

        ProcessInterruptAction(Consumer<Process> processConsumer) {
            this.processConsumer = processConsumer;
        }
    }
}
