package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TintHandler {

    @SubscribeEvent
    public static void onBlockColors(ColorHandlerEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        colors.register((state, worldIn, pos, tintIndex) -> {
            if(worldIn != null && pos != null) {
                TileEntity te = worldIn.getBlockEntity(pos);
                if(te instanceof SequencingSynthesizerBlockEntity) {
                    SequencingSynthesizerBlockEntity entity = (SequencingSynthesizerBlockEntity) te;
                    switch (tintIndex) {
                        case 1:
                            return entity.getDye().getColorValue();
                        case 2:
                            return 0x00FFD8;//0xDB3939
                    }
                }
            }
            return -1;
        }, BlockHandler.SEQUENCING_SYNTHESIZER.get());

        colors.register((state, worldIn, pos, index) -> index == 1 ? DyeColor.BLACK.getColorValue() : -1, BlockHandler.UNBUILT_SEQUENCING_SYNTHESIZER.get());

        colors.register((state, worldIn, pos, tintIndex) -> {
            if(worldIn != null && pos != null && tintIndex == 1) {
                TileEntity te = worldIn.getBlockEntity(pos);
                if(te instanceof EggPrinterBlockEntity) {
                    return ((EggPrinterBlockEntity) te).getDye().getColorValue();
                }
            }
            return -1;
        }, BlockHandler.EGG_PRINTER.get());
    }
}
