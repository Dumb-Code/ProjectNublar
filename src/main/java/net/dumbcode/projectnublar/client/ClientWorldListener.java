package net.dumbcode.projectnublar.client;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.EntityManagerListener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@RequiredArgsConstructor
public class ClientWorldListener implements IWorldEventListener {
    private final World world;
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(new ClientWorldListener(event.getWorld()));

    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
//        TileEntity te = this.world.getTileEntity(pos);
//        if(te instanceof BlockEntityElectricFencePole) {
//            ((BlockEntityElectricFencePole) te).renderList = -1;
//        }
    }

    @Override
    public void notifyLightSet(BlockPos pos) {

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1-1; x <= x1+1; x++) {
            for (int y = y1-1; y <= y1+1; y++) {
                for (int z = z1-1; z <= z1+1; z++) {
                    TileEntity te = this.world.getTileEntity(new BlockPos(x, y, z));
                    if(te instanceof BlockEntityElectricFencePole) {
                        ((BlockEntityElectricFencePole) te).renderList = -1;
                    }
                }
            }
        }
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void onEntityAdded(Entity entityIn) {

    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }
}
