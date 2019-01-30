package net.dumbcode.projectnublar.server.block.entity;

import net.minecraftforge.energy.EnergyStorage;

public class MachineModuleEnergyStorage extends EnergyStorage {
    public MachineModuleEnergyStorage(int capacity) {
        super(capacity);
    }

    public MachineModuleEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public MachineModuleEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public MachineModuleEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public void extractRaw(int amount) {
        this.energy -= Math.min(this.energy, amount);
    }

    public int getMaxReceive() {
        return this.maxReceive;
    }

    public int getMaxExtract() {
        return this.maxExtract;
    }
}
