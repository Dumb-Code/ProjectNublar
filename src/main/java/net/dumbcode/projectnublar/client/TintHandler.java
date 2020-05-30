package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID, value = Side.CLIENT)
public class TintHandler {

    @SubscribeEvent
    public static void onBlockColors(ColorHandlerEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        colors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if(worldIn != null && pos != null) {
                TileEntity te = worldIn.getTileEntity(pos);
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
        }, BlockHandler.SEQUENCING_SYNTHESIZER);

        colors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if(worldIn != null && pos != null && tintIndex == 1) {
                TileEntity te = worldIn.getTileEntity(pos);
                if(te instanceof EggPrinterBlockEntity) {
                    return ((EggPrinterBlockEntity) te).getDye().getColorValue();
                }
            }
            return -1;
        }, BlockHandler.EGG_PRINTER);
    }
}
