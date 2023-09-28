package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.text.WordUtils;

import java.util.function.Function;
import java.util.function.Supplier;

public class FossilHandler {
    public static final PostEarlyDeferredRegister<Fossil> DR = PostEarlyDeferredRegister.create(Fossil.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<Fossil>> FOSSIL_REGISTRY = DR.makeRegistry("fossils", () -> new RegistryBuilder<Fossil>()
            .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "ammonite")));

    public static RegistryObject<Fossil> AMMONITE = DR.register("ammonite", () -> new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fragmented/misc/overlay/ammonite"), "Ammonite", null, null)
            .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fragmented/misc/item/ammonite")));
    public static RegistryObject<Fossil> TRILOBITE = DR.register("trilobite", () -> new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fragmented/misc/overlay/ammonite"), "Trilobite", null, null)
            .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fragmented/misc/item/trilobite")));
    public static RegistryObject<Fossil> REX_FOOT = createSimpleFossilWithOneItemTexture("rex_foot", 83.6, 66, "misc/overlay/tyrannosaurus_foot", DinosaurHandler.TYRANNOSAURUS, "foot", "misc/item/tyrannosaurus_foot");
    public static RegistryObject<Fossil> REX_HAND = createSimpleFossilWithOneItemTexture("rex_hand", 83.6, 66, "misc/overlay/tyrannosaurus_hand", DinosaurHandler.TYRANNOSAURUS, "hand", "misc/item/tyrannosaurus_hand");
    public static RegistryObject<Fossil> REX_LEG = createSimpleFossilWithOneItemTexture("rex_leg", 83.6, 66, "misc/overlay/tyrannosaurus_leg", DinosaurHandler.TYRANNOSAURUS, "leg", "misc/item/tyrannosaurus_leg");
    public static RegistryObject<Fossil> REX_NECK = createSimpleFossilWithOneItemTexture("rex_neck", 83.6, 66, "misc/overlay/tyrannosaurus_neck", DinosaurHandler.TYRANNOSAURUS, "neck", "misc/item/tyrannosaurus_neck");
    public static RegistryObject<Fossil> REX_PELVIS = createSimpleFossilWithOneItemTexture("rex_pelvis", 83.6, 66, "misc/overlay/tyrannosaurus_pelvis", DinosaurHandler.TYRANNOSAURUS, "pelvis", "misc/item/tyrannosaurus_pelvis");
    public static RegistryObject<Fossil> REX_RIBCAGE = createSimpleFossilWithOneItemTexture("rex_ribcage", 83.6, 66, "misc/overlay/tyrannosaurus_ribcage", DinosaurHandler.TYRANNOSAURUS, "ribcage", "misc/item/tyrannosaurus_ribcage");
    public static RegistryObject<Fossil> REX_TAIL = createSimpleFossilWithOneItemTexture("rex_tail", 83.6, 66, "misc/overlay/tyrannosaurus_tail", DinosaurHandler.TYRANNOSAURUS, "tail", "misc/item/tyrannosaurus_tail");

    public static RegistryMap<Dinosaur, Fossil> FEATHERS = createSimpleFossilMap("feather", "block/fossil/fresh/common/item/feather", 150, 0, "common/item/feather");
    public static RegistryMap<Dinosaur, Fossil> FEET = createSimpleFossilMap("feet", "block/fossil/fresh/common/overlay/feet", 150, 0, "common/item/feet");
    public static RegistryMap<Dinosaur, Fossil> TOOTH = createSimpleFossilMap("tooth", "block/fossil/fresh/common/overlay/tooth", 150, 0, "common/item/tooth");

    private static RegistryObject<Fossil> createSimpleFossil(String name, double timeStart, double timeEnd, String blockOverlay, Supplier<Dinosaur> dinosaur, String partName, String itemTexture) {
        return DR.register(name, () -> new Fossil(
                timeStart, timeEnd, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fragmented/" + blockOverlay)
                , WordUtils.capitalizeFully(name.replace("_", " ")), dinosaur, partName)
                .withTexture(0.3, new ResourceLocation(ProjectNublar.MODID, "fragmented/" + itemTexture))
                .withTexture(0.6, new ResourceLocation(ProjectNublar.MODID, "fossilized/" + itemTexture))
                .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture)));
    };

    private static RegistryObject<Fossil> createSimpleFossilWithOneItemTexture(String name, double timeStart, double timeEnd, String blockOverlay, Supplier<Dinosaur> dinosaur, String partName, String itemTexture) {
        return DR.register(name, () -> new Fossil(
                timeStart, timeEnd, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fresh/" + blockOverlay)
                , WordUtils.capitalizeFully(name.replace("_", " ")), dinosaur, partName)
                .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture)));
    };

    private static RegistryMap<Dinosaur, Fossil> createSimpleFossilMap(String fossilName, String texture, double timeStart, double timeEnd, String itemTexture) {
        return createMap("%s_" + fossilName, dinosaur ->
                new Fossil(timeStart, timeEnd, null, new ResourceLocation(ProjectNublar.MODID, texture),
                        dinosaur.getFormattedName() + " " + WordUtils.capitalize(fossilName), () -> dinosaur, fossilName)
                        .withTexture(0.3, new ResourceLocation(ProjectNublar.MODID, "fragmented/" + itemTexture))
                        .withTexture(0.6, new ResourceLocation(ProjectNublar.MODID, "fossilized/" + itemTexture))
                        .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture)));
    }
    private static RegistryMap<Dinosaur, Fossil> createMap(String format, Function<Dinosaur, Fossil> supplier) {
        RegistryMap<Dinosaur, Fossil> map = new RegistryMap<>();
        DR.beforeRegister(() -> {
            for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
                map.putRegistry(dinosaur, DR.register(
                        String.format(format, dinosaur.getFormattedName()),
                        () -> supplier.apply(dinosaur)
                ));
            }
        });
        return map;
    }

}
