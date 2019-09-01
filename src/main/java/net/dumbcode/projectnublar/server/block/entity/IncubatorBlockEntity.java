package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.IncubatorGuiScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabListInformation;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class IncubatorBlockEntity extends MachineModuleBlockEntity<IncubatorBlockEntity> {

    public static int TOTAL_PLANT_MATTER = 100;

    @SideOnly(Side.CLIENT)
    public float movementTicks;
    @SideOnly(Side.CLIENT)
    public float[] snapshot = new float[7];
    @SideOnly(Side.CLIENT)
    public Egg activeEgg = null;
    @Getter
    @SideOnly(Side.CLIENT)
    private final List<Egg> eggList = Lists.newArrayList(
            new Egg(new Vec3d(9.0 / 16, 22.3/16, 2.9/16), new Vec3d(1, 0, 1))
    );

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
                new MachineProcess<>(this, new int[]{1}, new int[]{1}),
                new MachineProcess<>(this, new int[]{2}, new int[]{2}),
                new MachineProcess<>(this, new int[]{3}, new int[]{3}),
                new MachineProcess<>(this, new int[]{4}, new int[]{4}),
                new MachineProcess<>(this, new int[]{5}, new int[]{5}),
                new MachineProcess<>(this, new int[]{6}, new int[]{6})
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
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabListInformation info, int tab) {
        return new IncubatorGuiScreen(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(player, 84, 176,
                new MachineModuleSlot(this, 0, 80, 35),
                new MachineModuleSlot(this, 1, 109, 34),
                new MachineModuleSlot(this, 2, 93, 59),
                new MachineModuleSlot(this, 2, 64, 59),
                new MachineModuleSlot(this, 4, 50, 34),
                new MachineModuleSlot(this, 5, 65, 9),
                new MachineModuleSlot(this, 6, 94, 9));
    }

    // TODO: Change for balance, values are just for testing
    @Override
    public int getBaseEnergyProduction() {
        return 0;
    }

    @Override
    public int getBaseEnergyConsumption() {
        return 1;
    }

    @Override
    public int getEnergyCapacity() {
        return 1000;
    }

    @Override
    public int getEnergyMaxTransferSpeed() {
        return 50;
    }

    @Override
    public int getEnergyMaxExtractSpeed() {
        return 50;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public class Egg {
        private final Vec3d eggPosition;
        private final Vec3d pickupDirection;
        private float rotation;
        private float ticksSinceTurned;
    }

}
