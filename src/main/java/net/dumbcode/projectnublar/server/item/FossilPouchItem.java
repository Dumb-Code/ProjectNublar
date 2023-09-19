package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.containers.pouch.FossilPouchInventory;
import net.dumbcode.projectnublar.server.containers.pouch.FossilPouchMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Objects;
import java.util.UUID;

public class FossilPouchItem extends Item {
    private int width = 9;
    private int height = 3;


    public FossilPouchItem(Properties properties) {
        super(properties);
    }

    public static FossilPouchInventory getInv(ItemStack pouch) {
        return new FossilPouchInventory(pouch, getSackSize(pouch), 64);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
//        SackHelper.openSackGui(player, player.getItemInHand(hand));
        if (!level.isClientSide) {
            openScreen((ServerPlayerEntity) player, hand);
        }
        return super.use(level, player, hand);
    }

    public static void openScreen(ServerPlayerEntity user, Hand hand) {
        final ItemStack stack = user.getItemInHand(hand);
        // Getting existing UUID or generated new one
        UUID uuid = getOrBindUUID(stack);

        int width = getPouchWidth(stack);
        int height = getPouchHeight(stack);

        INamedContainerProvider provider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("projectnublar.fossil_pouch.menu.title");
            }

            @Override
            public Container createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new FossilPouchMenu(syncId, inv, width, height, uuid, stack);
            }
        };

        NetworkHooks.openGui(user, provider, (buf) -> {
            buf.writeInt(width);
            buf.writeInt(height);
            buf.writeUUID(uuid);
        });

    }


    public static UUID bindUid(ItemStack stack) {
        UUID uuid = UUID.randomUUID();
        stack.getOrCreateTag().putUUID("SackUUID", uuid);

        return uuid;
    }

    public static UUID getOrBindUUID(ItemStack stack) {
        UUID foundUid = getUUID(stack);

        if (foundUid == null) {
            return bindUid(stack);
        }

        return foundUid;
    }

    public static UUID getUUID(ItemStack stack) {
        try {
            return stack.getOrCreateTag().getUUID("SackUUID");
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isUUIDMatch(ItemStack stack, UUID uid) {
        UUID uuid = getUUID(stack);
        return uuid != null && uuid.equals(uid);
    }

    public static int getPouchWidth(ItemStack stack){
        return 9;
    }

    public static int getPouchHeight(ItemStack stack){
        return 3;
    }

    public static int getSackSize(ItemStack stack){
        return getPouchHeight(stack) * getPouchWidth(stack);
    }
}