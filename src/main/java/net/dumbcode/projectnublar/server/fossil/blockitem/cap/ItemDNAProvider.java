package net.dumbcode.projectnublar.server.fossil.blockitem.cap;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

public class ItemDNAProvider implements ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(ItemDNACap.class)
    public static Capability<ItemDNACap> SEAL;

    private ItemDNACap itemDNACap = null;
    private final LazyOptional<ItemDNACap> optional = LazyOptional.of(this::createCap);

    private ItemDNACap createCap() {
        if (this.itemDNACap == null) {
            this.itemDNACap = new ItemDNACap();
        }

        return this.itemDNACap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == SEAL) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        createCap().writeNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        createCap().readNBT(nbt);
    }

    @Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void attachSealCapability(AttachCapabilitiesEvent<ItemStack> event) {
            if (event.getObject().getItem() instanceof FossilItem) {
                event.addCapability(new ResourceLocation(ProjectNublar.MODID, "dna"), new ItemDNAProvider());
            }
        }
    }
}