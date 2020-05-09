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
import net.minecraft.nbt.*;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class DinosaurDropsComponent extends EntityComponent implements ItemDropComponent {

    private final List<String> fossilList = Lists.newArrayList();

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (String fossil : this.fossilList) {
            list.appendTag(new NBTTagString(fossil));
        }
        compound.setTag("drop_fossils", list);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.fossilList.clear();
        for (NBTBase base : compound.getTagList("drop_fossils", Constants.NBT.TAG_STRING)) {
            this.fossilList.add(((NBTTagString)base).getString());
        }
    }

    @Override
    public void collectItems(ComponentAccess access, Consumer<ItemStack> itemPlacer) {
        Dinosaur dino = access.get(ComponentHandler.DINOSAUR).orElseThrow(IllegalArgumentException::new).getDinosaur();
        for (String fossil : this.fossilList) {
            itemPlacer.accept(new ItemStack(ItemHandler.FOSSIL_ITEMS.get(dino).get(fossil)));
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
            for (JsonElement element : JsonUtils.getJsonArray(json, "fossils")) {
                this.fossilList.add(element.getAsString());
            }
        }
    }
}
