package net.dumbcode.projectnublar.server.fossil.blockitem.cap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ItemDNACap implements Capability.IStorage<ItemDNACap> {
    private int fossilTypeDNAMultiplier = 0;
    private int tierDNAMultiplier = 0;
    private int stoneTypeDNAMultiplier = 0;
    private int fossilDNAMultiplier = 0;

    public void copyFrom(ItemDNACap source) {
        this.fossilTypeDNAMultiplier = source.fossilTypeDNAMultiplier;
        this.tierDNAMultiplier = source.tierDNAMultiplier;
        this.stoneTypeDNAMultiplier = source.stoneTypeDNAMultiplier;
        this.fossilDNAMultiplier = source.fossilDNAMultiplier;
    }

    @Override
    public INBT writeNBT(Capability<ItemDNACap> capability, ItemDNACap instance, Direction side) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("fossilTypeDNAMultiplier", fossilTypeDNAMultiplier);
        nbt.putInt("tierDNAMultiplier", tierDNAMultiplier);
        nbt.putInt("stoneTypeDNAMultiplier", stoneTypeDNAMultiplier);
        nbt.putInt("fossilDNAMultiplier", fossilDNAMultiplier);
        return nbt;
    }

    public void writeNBT(CompoundNBT nbt) {
        nbt.putInt("fossilTypeDNAMultiplier", fossilTypeDNAMultiplier);
        nbt.putInt("tierDNAMultiplier", tierDNAMultiplier);
        nbt.putInt("stoneTypeDNAMultiplier", stoneTypeDNAMultiplier);
        nbt.putInt("fossilDNAMultiplier", fossilDNAMultiplier);
    }

    public void readNBT(CompoundNBT nbt) {
        fossilTypeDNAMultiplier = nbt.getInt("fossilTypeDNAMultiplier");
        tierDNAMultiplier = nbt.getInt("tierDNAMultiplier");
        stoneTypeDNAMultiplier = nbt.getInt("stoneTypeDNAMultiplier");
        fossilDNAMultiplier = nbt.getInt("fossilDNAMultiplier");
    }

    @Override
    public void readNBT(Capability<ItemDNACap> capability, ItemDNACap instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;
        fossilTypeDNAMultiplier = tag.getInt("fossilTypeDNAMultiplier");
        tierDNAMultiplier = tag.getInt("tierDNAMultiplier");
        stoneTypeDNAMultiplier = tag.getInt("stoneTypeDNAMultiplier");
        fossilDNAMultiplier = tag.getInt("fossilDNAMultiplier");
    }

    public int getFossilTypeDNAMultiplier() {
        return fossilTypeDNAMultiplier;
    }

    public void setFossilTypeDNAMultiplier(int fossilTypeDNAMultiplier) {
        this.fossilTypeDNAMultiplier = fossilTypeDNAMultiplier;
    }

    public int getTierDNAMultiplier() {
        return tierDNAMultiplier;
    }

    public void setTierDNAMultiplier(int tierDNAMultiplier) {
        this.tierDNAMultiplier = tierDNAMultiplier;
    }

    public int getStoneTypeDNAMultiplier() {
        return stoneTypeDNAMultiplier;
    }

    public void setStoneTypeDNAMultiplier(int stoneTypeDNAMultiplier) {
        this.stoneTypeDNAMultiplier = stoneTypeDNAMultiplier;
    }

    public int getFossilDNAMultiplier() {
        return fossilDNAMultiplier;
    }

    public void setFossilDNAMultiplier(int fossilDNAMultiplier) {
        this.fossilDNAMultiplier = fossilDNAMultiplier;
    }
}