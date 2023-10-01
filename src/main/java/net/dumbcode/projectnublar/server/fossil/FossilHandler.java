package net.dumbcode.projectnublar.server.fossil;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.text.WordUtils;

import java.util.function.Function;
import java.util.function.Supplier;

public class FossilHandler {
    public static final PostEarlyDeferredRegister<Fossil> DR = PostEarlyDeferredRegister.create(Fossil.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<Fossil>> FOSSIL_REGISTRY = DR.makeRegistry("fossils", () -> new RegistryBuilder<Fossil>()
            .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "ammonite")));

    public static RegistryObject<Fossil> AMMONITE = DR.register("ammonite", () -> new Fossil(201, 66, null, "trilobite", "Ammonite", null, null)
            .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fragmented/misc/")));
    public static RegistryObject<Fossil> TRILOBITE = DR.register("trilobite", () -> new Fossil(521, 320, null, "trilobite", "Trilobite", null, null)
            .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fragmented/misc/")));
    public static RegistryObject<Fossil> REX_FOOT = createSimpleFossilWithOneItemTexture("tyrannosaurus_foot", 83.6, 66, "tyrannosaurus_foot", DinosaurHandler.TYRANNOSAURUS, "foot", "misc/");
    public static RegistryObject<Fossil> REX_HAND = createSimpleFossilWithOneItemTexture("tyrannosaurus_hand", 83.6, 66, "tyrannosaurus_hand", DinosaurHandler.TYRANNOSAURUS, "hand", "misc/");
    public static RegistryObject<Fossil> REX_LEG = createSimpleFossilWithOneItemTexture("tyrannosaurus_leg", 83.6, 66, "tyrannosaurus_leg", DinosaurHandler.TYRANNOSAURUS, "leg", "misc/");
    public static RegistryObject<Fossil> REX_NECK = createSimpleFossilWithOneItemTexture("tyrannosaurus_neck_part", 83.6, 66, "tyrannosaurus_neck", DinosaurHandler.TYRANNOSAURUS, "neck", "misc/");
    public static RegistryObject<Fossil> REX_PELVIS = createSimpleFossilWithOneItemTexture("tyrannosaurus_pelvis", 83.6, 66, "tyrannosaurus_pelvis", DinosaurHandler.TYRANNOSAURUS, "pelvis", "misc/");
    public static RegistryObject<Fossil> REX_RIBCAGE = createSimpleFossilWithOneItemTexture("tyrannosaurus_ribcage", 83.6, 66, "tyrannosaurus_ribcage", DinosaurHandler.TYRANNOSAURUS, "ribcage", "misc/");
    public static RegistryObject<Fossil> REX_TAIL = createSimpleFossilWithOneItemTexture("tyrannosaurus_tail", 83.6, 66, "tyrannosaurus_tail", DinosaurHandler.TYRANNOSAURUS, "tail", "misc/");

    public static RegistryMap<Dinosaur, Fossil> FEATHERS = createSimpleFossilMap("feather", 150, 0, "common/", "feather");
    public static RegistryMap<Dinosaur, Fossil> FEET = createSimpleFossilMap("feet", 150, 0, "common/", "feet");
    public static RegistryMap<Dinosaur, Fossil> TOOTH = createSimpleFossilMap("tooth", 150, 0, "common/", "tooth");

    private static RegistryObject<Fossil> createSimpleFossil(String name, double timeStart, double timeEnd, String textureName, Supplier<Dinosaur> dinosaur, String partName, String itemTexture) {
        return DR.register(name, () -> new Fossil(
                timeStart, timeEnd, null, textureName
                , WordUtils.capitalizeFully(name.replace("_", " ")), dinosaur, partName)
                .withTextures(
                        new Pair<>(0.3, new ResourceLocation(ProjectNublar.MODID, "fragmented/" + itemTexture)),
                        new Pair<>(0.6, new ResourceLocation(ProjectNublar.MODID, "fossilized/" + itemTexture)),
                        new Pair<>(1D, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture))
                ));
    };

    private static RegistryObject<Fossil> createSimpleFossilWithOneItemTexture(String name, double timeStart, double timeEnd, String textureName, Supplier<Dinosaur> dinosaur, String partName, String itemTexture) {
        return DR.register(name, () -> new Fossil(
                timeStart, timeEnd, null, textureName
                , WordUtils.capitalizeFully(name.replace("_", " ")), dinosaur, partName)
                .withTexture(1, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture)));
    };

    private static RegistryMap<Dinosaur, Fossil> createSimpleFossilMap(String fossilName, double timeStart, double timeEnd, String itemTexture, String textureName) {
        return createMap("%s_" + fossilName, dinosaur ->
                new Fossil(timeStart, timeEnd, null, textureName, 
                        dinosaur.getFormattedName() + " " + WordUtils.capitalize(fossilName), () -> dinosaur, fossilName)
                        .withTextures(
                                new Pair<>(0.3, new ResourceLocation(ProjectNublar.MODID, "fragmented/" + itemTexture)),
                                new Pair<>(0.6, new ResourceLocation(ProjectNublar.MODID, "fossilized/" + itemTexture)),
                                new Pair<>(1D, new ResourceLocation(ProjectNublar.MODID, "fresh/" + itemTexture))
                        ));
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
