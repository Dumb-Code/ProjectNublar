package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

// TODO: move somewhere else?
@Deprecated
public class Fossils {
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
