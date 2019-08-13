package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DinosaurEggItem extends BasicDinosaurItem {
    public DinosaurEggItem(Dinosaur dinosaur) {
        super(dinosaur);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            DinosaurEntity entity = this.getDinosaur().createEntity(worldIn);

            entity.get(EntityComponentTypes.GENDER).ifPresent(c -> c.male = worldIn.rand.nextBoolean());

            entity.setLocationAndAngles(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F), 0.0F);
            entity.rotationYawHead = entity.rotationYaw;
            entity.renderYawOffset = entity.rotationYaw;

            worldIn.spawnEntity(entity);
        }
        return EnumActionResult.SUCCESS;
    }
}
