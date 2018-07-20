package net.dumbcode.projectnublar.server.dinosaur;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.render.model.DinosaurModelContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {
    @GameRegistry.ObjectHolder(ProjectNublar.MODID + ":velociraptor")
    public static Dinosaur MISSING = null;

    private final ModelProperties modelProperties = new ModelProperties();

    private DinosaurModelContainer modelContainer;


    private int cookedMeatHealAmount;
    private int rawMeatHealAmount;
    private float cookedMeatSaturation;
    private float rawMeatSaturation;

    @Setter(AccessLevel.NONE)
    private ItemStack rawMeat;
    @Setter(AccessLevel.NONE)
    private ItemStack cookedMeat;

    public ItemStack getRawMeat() {
        if(rawMeat == null) {
            rawMeat = ItemDinosaurMeat.createMeat(this, ItemDinosaurMeat.CookState.RAW);
        }
        return rawMeat;
    }

    public ItemStack getCookedMeat() {
        if(rawMeat == null) {
            rawMeat = ItemDinosaurMeat.createMeat(this, ItemDinosaurMeat.CookState.COOKED);
        }
        return rawMeat;
    }

    public String getOreSuffix() {
        return toCamelCase(getRegName().getResourcePath());
    }

    private String toCamelCase(String snakeCase) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i<snakeCase.length();i++) {
            char c = snakeCase.charAt(i);
            if(c == '_') {
                if(i+1 < snakeCase.length()) {
                    builder.append(Character.toUpperCase(snakeCase.charAt(i+1)));
                    i++;
                }
            } else {
                if(i == 0)
                    builder.append(Character.toUpperCase(c));
                else
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    @Nonnull //A quick nonnull registry name. Usefull to prevent complier warnings
    public ResourceLocation getRegName() {
        if(this.getRegistryName() == null) {
            throw new RuntimeException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }
}
