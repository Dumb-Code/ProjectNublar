package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.IncubatorGuiScreen;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.IncubatorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class IncubatorBlockEntity extends MachineModuleBlockEntity<IncubatorBlockEntity> {

    public static int TOTAL_PLANT_MATTER = 100;

    @Getter
    @Setter
    private double plantMatter;

    @Override
    protected int getInventorySize() {
        return 7;
    }

    @Override
    protected List<MachineRecipe<IncubatorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(IncubatorRecipe.INSTANCE);
    }

    @Override
    protected IncubatorBlockEntity asB() {
        return this;
    }

    @Override
    public void update() {
        super.update();
        if(this.plantMatter < TOTAL_PLANT_MATTER) {
            this.plantMatter = Math.min(TOTAL_PLANT_MATTER, this.plantMatter + MachineUtils.getPlantMatter(this.handler.getStackInSlot(0).splitStack(1), this.world, this.pos));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setDouble("PlantMatter", this.plantMatter);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.plantMatter = compound.getDouble("PlantMatter");
        super.readFromNBT(compound);
    }

    @Override
    protected List<MachineProcess<IncubatorBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(new int[]{1}, new int[]{1}),
                new MachineProcess<>(new int[]{2}, new int[]{2}),
                new MachineProcess<>(new int[]{3}, new int[]{3}),
                new MachineProcess<>(new int[]{4}, new int[]{4}),
                new MachineProcess<>(new int[]{5}, new int[]{5}),
                new MachineProcess<>(new int[]{6}, new int[]{6})
        );
    }



    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        if(slot == 0) {
            return MachineUtils.getPlantMatter(stack, this.world, this.pos) > 0;
        }
        return super.isItemValidFor(slot, stack);
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player, int tab) {
        return new IncubatorGuiScreen(player, this, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        MachineModuleSlot[] slots = new MachineModuleSlot[7];
        slots[0] =  new MachineModuleSlot(this, 0, 79, 40);
        float radius = 50;
        for (int i = 0; i < 6; i++) {
            slots[i + 1] = new MachineModuleSlot(this, i + 1, 79 + (int) (radius * Math.cos(i * 1.0472D)), 40 + (int) (radius * Math.sin(i * 1.0472D)));
        }
        return new MachineModuleContainer(player, 112, 176, slots);
    }
}
