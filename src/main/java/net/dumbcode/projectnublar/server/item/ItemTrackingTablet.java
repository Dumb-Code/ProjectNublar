package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.S2COpenTablet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemTrackingTablet extends Item {
    public ItemTrackingTablet(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if(!world.isClientSide) {
            ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new S2COpenTablet(hand));
        }
        return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
    }
}
