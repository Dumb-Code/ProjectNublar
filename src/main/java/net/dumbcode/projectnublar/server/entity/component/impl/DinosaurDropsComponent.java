package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ItemDropComponent;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class DinosaurDropsComponent extends EntityComponent implements ItemDropComponent {

    private final List<String> fossilList = Lists.newArrayList();

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (String fossil : this.fossilList) {
            list.add(StringNBT.valueOf(fossil));
        }
        compound.put("drop_fossils", list);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.fossilList.clear();
        for (INBT base : compound.getList("drop_fossils", Constants.NBT.TAG_STRING)) {
            this.fossilList.add(base.toString());
        }
    }

    @Override
    public void collectItems(ComponentAccess access, Consumer<ItemStack> itemPlacer) {
        Dinosaur dino = access.get(ComponentHandler.DINOSAUR).orElseThrow(IllegalArgumentException::new).getDinosaur();
        for (String fossil : this.fossilList) {
            itemPlacer.accept(new ItemStack(ItemHandler.FOSSIL_ITEMS.get(dino).get(fossil).get()));
        }
    }

    @Accessors(chain = true)
    @Setter
    @Getter
    public static class Storage implements EntityComponentStorage<DinosaurDropsComponent> {

        private final List<String> fossilList = Lists.newArrayList();


        public Storage addFossils(String... fossils) {
            Collections.addAll(this.fossilList, fossils);
            return this;
        }

        @Override
        public void constructTo(DinosaurDropsComponent component) {

            component.fossilList.addAll(this.fossilList);

        }

        @Override
        public void writeJson(JsonObject json) {
            JsonArray jarr = new JsonArray();
            for (String fossil : this.fossilList) {
                jarr.add(fossil);
            }
            json.add("fossils", jarr);
        }

        @Override
        public void readJson(JsonObject json) {
            this.fossilList.clear();
            for (JsonElement element : JSONUtils.getAsJsonArray(json, "fossils")) {
                this.fossilList.add(element.getAsString());
            }
        }
    }
}
