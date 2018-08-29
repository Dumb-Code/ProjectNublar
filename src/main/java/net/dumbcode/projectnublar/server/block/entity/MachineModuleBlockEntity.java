package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

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
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stateID = compound.getInteger("StateID");
    }

    @Override
    public void update() {
        for (Process process : this.processes) {
            if(this.canProcess(process)) {
                if(process.isProcessing() || this.searchForRecipes(process)) {
                    if(process.isFinished()) {
                        for (MachineRecipe<B> recipe : this.recipes) {
                            if(recipe.accpets(this.asB, process)) { //TODO: maybe move the recipes to a registry, and cache them instead ?
                                recipe.onRecipeFinished(this.asB, process);
                                process.setTime(0);
                                process.setProcessing(false);
                                this.searchForRecipes(process);
                            }
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
                process.setTotalTime(recipe.getRecipeTime(this.asB, process));
                this.markDirty();
                return true;
            }
        }
        return false;
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

        int totalTime;
        int time;
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
