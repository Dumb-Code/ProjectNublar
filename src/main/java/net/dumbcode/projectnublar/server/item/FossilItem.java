package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.FossilInformation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class FossilItem extends Item implements DinosaurStack {

    @Getter
    private final Dinosaur dinosaur;
    @Getter
    private final FossilInformation information;
    private final TextComponentTranslation stackDisplayName;

    public FossilItem(Dinosaur dinosaur, FossilInformation information) {
        this.dinosaur = dinosaur;
        this.information = information;
        this.stackDisplayName = new TextComponentTranslation(ProjectNublar.MODID+".item.fossil.name", information.getDinosaur().createNameComponent(), information.getType());
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return stackDisplayName.getUnformattedText();
    }

    @Override
    public Dinosaur getValue(ItemStack stack) {
        return dinosaur;
    }

    @Override
    public ItemStack getItemStack(Dinosaur dinosaur) {
        return new ItemStack(this);
    }

    @Override
    public Map<Object, ResourceLocation> getModelResourceLocations(Dinosaur dinosaur) {
        Map<Object, ResourceLocation> map = Maps.newHashMap();
        ResourceLocation dinoreg = dinosaur.getRegName();
        for(String bone : dinosaur.getSkeletalInformation().getIndividualBones()) {
            map.put(bone, new ResourceLocation(dinoreg.getResourceDomain(), "item/" + "bones/" + dinoreg.getResourcePath()+ "/" + bone));
        }
        return map;
    }

    @Override
    public Object getVarient(ItemStack stack) {
        return information.getType();
    }
}
