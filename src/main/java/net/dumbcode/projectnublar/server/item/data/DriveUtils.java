package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Random;

public class DriveUtils {

    public static void addItemToDrive(ItemStack drive, ItemStack inItem) {

        if (!(inItem.getItem() instanceof DriveInformation)) {
            return;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();

        List<Integer> list = Lists.newArrayList();
        for (int i1 = 0; i1 < 50; i1++) {
            int[] aint = MathUtils.generateWeightedList(info.getSize(inItem));
            list.add(aint[new Random().nextInt(aint.length)]);
        }
        list.sort(null);
        for (Integer integer : list) {
            System.out.println(integer.intValue());
        }

        int[] aint = MathUtils.generateWeightedList(info.getSize(inItem));
        //Fisher–Yates shuffle
        Random rnd = new Random();
        for (int i = aint.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            int a = aint[index];
            aint[index] = aint[i];
            aint[i] = a;
        }

        int result = aint[new Random().nextInt(aint.length)];



        System.out.println(result);

        String key = info.getKey(inItem);

        if(key.isEmpty()) {
            return;
        }
        NBTTagCompound nbt = drive.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");

        NBTTagCompound inner = nbt.getCompoundTag(key);
        inner.setInteger("amount", inner.getInteger("amount") + result);
        nbt.setTag(key, inner);

        drive.getOrCreateSubCompound(ProjectNublar.MODID).setTag("drive_information", nbt);
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
    }

}
