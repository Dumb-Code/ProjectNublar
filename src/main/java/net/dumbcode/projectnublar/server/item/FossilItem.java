package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

public class FossilItem extends Item implements DinosaurStack {

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FossilInfomation info = getFossilInfomation(stack);
        return info.getDinosaur().getRegName() + " " + info.getType();
    }

    @Override
    public Dinosaur getValue(ItemStack stack) {
        return getFossilInfomation(stack).getDinosaur();
    }

    @Override
    public ItemStack getItemStack(Dinosaur dinosaur) {
        return createNewStack(new FossilInfomation(dinosaur, dinosaur.getSkeletalInfomation().getIndividualBones().get(0)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subtypes) {
        if(this.isInCreativeTab(tab)) {
            FossilInfomation.getAllInfomation().stream().map(this::createNewStack).forEach(subtypes::add);
        }
    }

    @Override
    public Map<Object, ResourceLocation> getModelResourceLocations(Dinosaur dinosaur) {
        Map<Object, ResourceLocation> map = Maps.newHashMap();
        ResourceLocation dinoreg = dinosaur.getRegName();
        for(String bone : dinosaur.getSkeletalInfomation().getIndividualBones()) {
            map.put(bone, new ResourceLocation(dinoreg.getResourceDomain(), "item/" + "bones/" + dinoreg.getResourcePath()+ "/" + bone));
        }
        return map;
    }

    @Override
    public Object getVarient(ItemStack stack) {
        return getFossilInfomation(stack).getType();
    }

    public static FossilInfomation getFossilInfomation(ItemStack stack) {
        NBTTagCompound nbt = stack.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("Fossil Info");
        return new FossilInfomation(ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("Dinosaur"))), nbt.getString("Bone Type"));
    }

    public static ItemStack putFossilInfomation(ItemStack stack, FossilInfomation infomation) {
        NBTTagCompound compound = stack.getOrCreateSubCompound(ProjectNublar.MODID);
        NBTTagCompound nbt = compound.getCompoundTag("Fossil Info");
        nbt.setString("Dinosaur", infomation.getDinosaur().getRegName().toString());
        nbt.setString("Bone Type", infomation.getType());
        compound.setTag("Fossil Info", nbt);
        return stack;
    }

    public ItemStack createNewStack(FossilInfomation fossilInfomation) {
        return putFossilInfomation(new ItemStack(this), fossilInfomation);
    }

    @Value
    public static class FossilInfomation {
        private final Dinosaur dinosaur;
        private final String type;
        public static List<FossilInfomation> getAllInfomation() {
            List<FossilInfomation> list = Lists.newArrayList();
            ProjectNublar.DINOSAUR_REGISTRY.forEach(dino -> Lists.newArrayList(dino.getSkeletalInfomation().getIndividualBones()).forEach(bone -> list.add(new FossilInfomation(dino, bone))));
            return list;
        }
    }
}
