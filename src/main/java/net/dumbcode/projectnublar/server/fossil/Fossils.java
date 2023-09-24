package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlock;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Fossils {
    public static final List<Fossil> FOSSILS = new ArrayList<>();
    public static final Multimap<RegistryObject<Dinosaur>, Item> ITEMS = Multimaps.newListMultimap(new HashMap<>(), () -> new ArrayList<>());
    public static final List<StoneType> STONE_TYPES = new ArrayList<>();

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ProjectNublar.MODID)
    public static class Events {

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Pre event) {
            if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
                for (RegistryObject<Fossil> fossilReg : FossilHandler.REGISTER.getEntries()) {
                    Fossil fossil = fossilReg.get();
                    if (fossil.texture != null) {
                        event.addSprite(fossil.texture);
                    }
                    if (fossil.itemTexture != null) {
                        event.addSprite(fossil.itemTexture);
                    }
                }
            }
        }
    }
}
