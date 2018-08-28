package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class BlockHandler {

    public static final SkeletalBuilderBlock SKELETAL_BUILDER = getNonNull();
    public static final Map<Dinosaur, FossilBlock> FOSSIlS = new HashMap<>();


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new SkeletalBuilderBlock().setRegistryName("skeletal_builder").setUnlocalizedName("skeletal_builder")
        );

        populateMap(event, FOSSIlS, "%s_fossil", FossilBlock::new);
    }

    private static <T extends Block> void populateMap(RegistryEvent.Register<Block> event, Map<Dinosaur, T> itemMap, String dinosaurRegname, Function<Dinosaur, T> supplier) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            if(dinosaur != Dinosaur.MISSING) {
                T item = supplier.apply(dinosaur);
                String name = String.format(dinosaurRegname, dinosaur.getFormattedName());
                item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                item.setUnlocalizedName(name);
                item.setCreativeTab(ProjectNublar.TAB);
                itemMap.put(dinosaur, item);
                event.getRegistry().register(item);
            }
        }
    }

    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        populateNestedMap(event, itemMap, getterFunction, Object::toString, creationFunc, dinosaurRegname);
    }

    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, Function<S, String> toStringFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            if(dinosaur != Dinosaur.MISSING) {
                for (S s : getterFunction.apply(dinosaur)) {
                    T item = creationFunc.apply(dinosaur, s);
                    String name = String.format(dinosaurRegname, dinosaur.getFormattedName(), toStringFunction.apply(s));
                    item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                    item.setUnlocalizedName(name);
                    item.setCreativeTab(ProjectNublar.TAB);
                    itemMap.computeIfAbsent(dinosaur, d -> new HashMap<>()).put(s, item);
                    event.getRegistry().register(item);
                }
            }
        }
    }

//    @Nonnull//TODO: fix
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
