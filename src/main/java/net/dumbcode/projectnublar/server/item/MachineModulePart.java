package net.dumbcode.projectnublar.server.item;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@Value
@Builder
public class MachineModulePart {
    @NonNull
    private final MachineModuleType type;
    @Singular
    private final List<Predicate<ItemStack>> tiers;
    @Singular
    private final List<MachineModuleType> dependencies;

    public boolean testDependents(int newValue, ToIntFunction<MachineModuleType> tierGetter) {
        for (MachineModuleType dependent : this.dependencies) {
            if(dependent != null && tierGetter.applyAsInt(dependent) < newValue) {
                return false;
            }
        }
        return true;
    }

    public int getTierFromStack(ItemStack stack) {
        for (int i = 0; i < this.tiers.size(); i++) {
            if(this.tiers.get(i).test(stack)) {
                return i + 1;
            }
        }
        return 0;
    }

    public String getName() {
        return this.type.getName().toLowerCase();
    }

    public int getTiers() {
        return this.tiers.size();
    }

}
