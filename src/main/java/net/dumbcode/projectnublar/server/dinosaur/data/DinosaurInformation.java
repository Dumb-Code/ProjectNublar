package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;

@Data
public class DinosaurInformation {
    private DinosaurPeriod period;
    private final List<BiomeDictionary.Type> biomeTypes = Lists.newArrayList();
    private boolean canClimb;
}
