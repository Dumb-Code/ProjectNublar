package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.IncubatorGuiScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.recipes.IncubatorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class IncubatorBlockEntity extends MachineModuleBlockEntity<IncubatorBlockEntity> {

    public static final int DEFAULT_PLANT_MATTER = 100;
    public static final int HALF_EGG_SIZE = 8;
    public static final int EGG_PADDING = 15;

    public static final int TICKS_TO_OPEN = 20;
    public static final int TICKS_LID_WAIT_TO_CLOSE = 30;

    public float movementTicks = 100000;
    public int[] lidTicks = new int[3];
    public float[] snapshot = new float[7];
    public int[] activeEgg = { -1, 0 };
    @Getter private final Egg[] eggList = new Egg[9];

    @Getter
    @Setter
    private double plantMatter;

    @Getter
    private double totalPlantMatter = DEFAULT_PLANT_MATTER;

    @Override
    public void tiersUpdated() {
        this.totalPlantMatter = DEFAULT_PLANT_MATTER * this.getTierModifier(MachineModuleType.TANKS, 0.5F);
    }

    @Override
    protected int getInventorySize() {
        return 10;
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
        if(this.plantMatter < this.totalPlantMatter) {
            this.plantMatter = Math.min(this.totalPlantMatter, this.plantMatter + MachineUtils.getPlantMatter(this.handler.getStackInSlot(0).splitStack(1), this.world, this.pos));
        }

        this.movementTicks++;

        this.lidTicks[1] = this.lidTicks[0];
        if (!this.getOpenedUsers().isEmpty() || this.activeEgg[0] != -1 || this.lidTicks[2]-- > 0) {
            if(this.lidTicks[0] < TICKS_TO_OPEN) {
                this.lidTicks[0]++;
            }
            if(this.activeEgg[0] != -1) {
                this.lidTicks[2] = TICKS_LID_WAIT_TO_CLOSE;
            }
        } else if(this.lidTicks[0] > 0) {
            this.lidTicks[0]--;
        }
    }

    public List<Egg> getCollidingEggs(int x, int y) {
        List<Egg> eggs = new ArrayList<>();
        for (Egg egg : this.eggList) {
            if(egg != null && Math.abs(egg.xPos - x) < 2*(HALF_EGG_SIZE)+EGG_PADDING/2 && Math.abs(egg.yPos - y) < 2*(HALF_EGG_SIZE)+EGG_PADDING/2) {
                eggs.add(egg);
            }
        }
        return eggs;
    }

    public boolean canPlaceEggAt(int x, int y) {
        int amount = 0;
        for (Egg egg : this.eggList) {
            if(egg != null) {
                amount++;
            }
        }
        return this.getCollidingEggs(x, y).isEmpty() && amount < 3*(this.getTier(MachineModuleType.CONTAINER)+1);
    }

    public void placeEgg(int x, int y, ItemStack stack) {
        if(this.canPlaceEggAt(x, y) && stack.getItem() instanceof DinosaurProvider) {
            List<DinosaurEggType> eggTypes = ((DinosaurProvider) stack.getItem()).getDinosaur().getAttacher().getStorage(ComponentHandler.DINOSAUR_EGG_LAYING).getEggTypes();
            DinosaurEggType type = eggTypes.get(this.world.rand.nextInt(eggTypes.size()));

            Vec3d armPos = new Vec3d(1.6, 1.4, 0.5);
            Vec3d position = new Vec3d(4D + 12D*x/100D, 22.2, 2D + 12D*y/100D).scale(1/16F);

            double handLength = 4 / 16F; // BlockEntityIncubatorRenderer#HAND_JOIN.length

            Vec3d normal;
            int times = 0;
            do {
                normal = armPos.subtract(position.x, 1.4 - (0.2+this.world.rand.nextFloat()*0.85), position.z)
                    .rotateYaw((float) ((this.world.rand.nextFloat()-0.5D)*4D*Math.PI)).normalize();
            } while(armPos.distanceTo(position.add(normal.scale(handLength))) > 1.1 && times++ < 5000);

            if(times >= 5000) {
                ProjectNublar.getLogger().warn("Invalid placement position for egg. Contact DumbCode and tell them to adjust the distance limit.");
                return;
            }
            for (int i = 0; i < this.eggList.length; i++) {
                if(this.eggList[i] == null) {
                    this.eggList[i] = new Egg(x, y, position, normal, type);
                    this.handler.setStackInSlot(i + 1, stack.splitStack(1));
                    break;
                }
            }

        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setDouble("PlantMatter", this.plantMatter);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.eggList.length; i++) {
            Egg egg = this.eggList[i];
            if(egg != null) {
                NBTTagCompound nbt = new NBTTagCompound();

                nbt.setByte("Slot", (byte) i);

                nbt.setByte("XPos", (byte) egg.xPos);
                nbt.setByte("YPos", (byte) egg.yPos);

                nbt.setDouble("EggPositionX", egg.eggPosition.x);
                nbt.setDouble("EggPositionY", egg.eggPosition.y);
                nbt.setDouble("EggPositionZ", egg.eggPosition.z);

                nbt.setDouble("PickupDirectionX", egg.pickupDirection.x);
                nbt.setDouble("PickupDirectionY", egg.pickupDirection.y);
                nbt.setDouble("PickupDirectionZ", egg.pickupDirection.z);

                nbt.setTag("EggType", DinosaurEggType.writeToNBT(egg.eggType));

                list.appendTag(nbt);
            }
        }
        compound.setTag("Eggs", list);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.plantMatter = compound.getDouble("PlantMatter");

        NBTTagList eggs = compound.getTagList("Eggs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < eggs.tagCount(); i++) {
            NBTTagCompound t = eggs.getCompoundTagAt(i);
            this.eggList[t.getByte("Slot")] = new Egg(
                t.getByte("XPos"), t.getByte("YPos"),
                new Vec3d(t.getDouble("EggPositionX"), t.getDouble("EggPositionY"), t.getDouble("EggPositionZ")),
                new Vec3d(t.getDouble("PickupDirectionX"), t.getDouble("PickupDirectionY"), t.getDouble("PickupDirectionZ")),
                DinosaurEggType.readFromNBT(t.getCompoundTag("EggType"))
            );
        }
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
            new MachineProcess<>(this, new int[]{6}, new int[]{6}),
            new MachineProcess<>(this, new int[]{7}, new int[]{7}),
            new MachineProcess<>(this, new int[]{8}, new int[]{8}),
            new MachineProcess<>(this, new int[]{9}, new int[]{9})
        );
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos()).grow(1, 0, 1).expand(0, 2, 0);
    }

    @Override
    public int[] constantInputSlots() {
        return new int[] { 0 };
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        if(slot == 0) {
            return MachineUtils.getPlantMatter(stack, this.world, this.pos) > 0;
        }
        return super.isItemValidFor(slot, stack);
    }

    @Override
    public int slotSize(int slot) {
        if(slot > 0) {
            return 1; //Eggs
        }
        return super.slotSize(slot);
    }

    @Override
    protected void onSlotChanged(int slot) {
        if(slot > 0 && this.handler.getStackInSlot(slot).isEmpty()) {
            this.eggList[slot-1] = null;
        }
        super.onSlotChanged(slot);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return new IncubatorGuiScreen(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(this, player, 117, 176,
            new MachineModuleSlot(this, 0, 8, 96),
            new MachineModuleSlot(this, 1, 0, 0),
            new MachineModuleSlot(this, 2, 0, 0),
            new MachineModuleSlot(this, 3, 0, 0),
            new MachineModuleSlot(this, 4, 0, 0),
            new MachineModuleSlot(this, 5, 0, 0),
            new MachineModuleSlot(this, 6, 0, 0),
            new MachineModuleSlot(this, 7, 0, 0),
            new MachineModuleSlot(this, 8, 0, 0),
            new MachineModuleSlot(this, 9, 0, 0)
        );
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
    public static class Egg {
        private final int xPos;
        private final int yPos;
        private final Vec3d eggPosition;
        private final Vec3d pickupDirection;
        private final DinosaurEggType eggType;
        private float rotation;
        private float rotationStart;
        private float ticksSinceTurned;
    }

}
