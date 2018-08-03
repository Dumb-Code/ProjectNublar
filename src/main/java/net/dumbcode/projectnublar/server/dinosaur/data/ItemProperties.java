package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.gson.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

@Data
@Accessors(chain = true)
public class ItemProperties {

    private final ItemEggColor maleEggColor = new ItemEggColor();
    private final ItemEggColor femaleEggColor = new ItemEggColor();

    private int cookedMeatHealAmount;
    private int rawMeatHealAmount;
    private float cookedMeatSaturation;
    private float rawMeatSaturation;
    private float cookingExperience;

    public void copyFrom(ItemProperties other) {
        maleEggColor.setPrimary(other.maleEggColor.getPrimary());
        maleEggColor.setSecondary(other.maleEggColor.getSecondary());
        femaleEggColor.setPrimary(other.femaleEggColor.getPrimary());
        femaleEggColor.setSecondary(other.femaleEggColor.getSecondary());

        cookedMeatHealAmount = other.cookedMeatHealAmount;
        cookedMeatSaturation = other.cookedMeatSaturation;
        rawMeatHealAmount = other.rawMeatHealAmount;
        rawMeatSaturation = other.rawMeatSaturation;
        cookingExperience = other.cookingExperience;
    }

    public static class JsonAdapter implements JsonSerializer<ItemProperties>, JsonDeserializer<ItemProperties> {

        @Override
        public ItemProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            ItemProperties properties = new ItemProperties();
            properties.setCookingExperience(object.get("cooking_experience").getAsFloat());

            JsonObject rawMeatData = object.getAsJsonObject("raw_meat");
            JsonObject cookedMeatData = object.getAsJsonObject("cooked_meat");

            properties
                    .setCookedMeatHealAmount(cookedMeatData.get("heal_amount").getAsInt())
                    .setCookedMeatSaturation(cookedMeatData.get("saturation").getAsFloat())
                    .setRawMeatHealAmount(rawMeatData.get("heal_amount").getAsInt())
                    .setRawMeatSaturation(rawMeatData.get("saturation").getAsFloat())
            ;

            JsonObject maleEgg = object.getAsJsonObject("male_egg");
            JsonObject femaleEgg = object.getAsJsonObject("female_egg");
            properties.maleEggColor.setPrimary(maleEgg.get("primary_color").getAsInt());
            properties.maleEggColor.setSecondary(maleEgg.get("secondary_color").getAsInt());
            properties.femaleEggColor.setPrimary(femaleEgg.get("primary_color").getAsInt());
            properties.femaleEggColor.setSecondary(femaleEgg.get("secondary_color").getAsInt());
            return properties;
        }

        @Override
        public JsonElement serialize(ItemProperties properties, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("cooking_experience", properties.getCookingExperience());

            JsonObject rawMeatData = new JsonObject();
            JsonObject cookedMeatData = new JsonObject();

            rawMeatData.addProperty("heal_amount", properties.getRawMeatHealAmount());
            rawMeatData.addProperty("saturation", properties.getRawMeatSaturation());
            cookedMeatData.addProperty("heal_amount", properties.getCookedMeatHealAmount());
            cookedMeatData.addProperty("saturation", properties.getCookedMeatSaturation());
            object.add("raw_meat", rawMeatData);
            object.add("cooked_meat", cookedMeatData);

            JsonObject maleEgg = new JsonObject();
            JsonObject femaleEgg = new JsonObject();
            femaleEgg.addProperty("primary_color", properties.getFemaleEggColor().getPrimary());
            femaleEgg.addProperty("secondary_color", properties.getFemaleEggColor().getSecondary());
            maleEgg.addProperty("primary_color", properties.getMaleEggColor().getPrimary());
            maleEgg.addProperty("secondary_color", properties.getMaleEggColor().getSecondary());

            object.add("female_egg", femaleEgg);
            object.add("male_egg", maleEgg);
            return object;
        }
    }

}
