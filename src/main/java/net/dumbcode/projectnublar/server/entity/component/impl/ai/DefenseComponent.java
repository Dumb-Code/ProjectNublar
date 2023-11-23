package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JSONUtils;

@Getter
public class DefenseComponent extends EntityComponent {

    private final ModifiableField defense = new ModifiableField();

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.put("defense", this.defense.writeToNBT());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        this.defense.readFromNBT(compound.getCompound("defense"));
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<DefenseComponent> {

        private double baseDefense;

        @Override
        public void constructTo(DefenseComponent component) {
            component.defense.setBaseValue(this.baseDefense);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("defense", this.baseDefense);
        }

        @Override
        public void readJson(JsonObject json) {
            this.baseDefense = JSONUtils.getAsFloat(json, "defense");
        }
    }
}
