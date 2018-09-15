package net.dumbcode.projectnublar.server.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.GenderComponent;
import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Locale;

public class DinosaurSpawnEgg extends Item implements DinosaurProvider {

    @Getter
    private final Dinosaur dinosaur;

    public DinosaurSpawnEgg(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(player.isSneaking()) {
            SpawnEggInfo info = SpawnEggInfo.fromStack(stack);
            info.setState(info.getState().next());
            if (world.isRemote) {
                String modeString;
                switch (info.getState()) {
                    case MALE: modeString = "male"; break;
                    case FEMALE: modeString = "female"; break;
                    default: modeString = "random"; break;
                }
                player.sendMessage(new TextComponentTranslation("spawnegg.genderchange." + modeString));
            }
            stack.getOrCreateSubCompound(ProjectNublar.MODID).setTag("SpawnEggInfo", info.serialize());
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (!player.canPlayerEdit(pos.offset(side), side, stack)) {
            return EnumActionResult.PASS;
        } else {
            IBlockState state = world.getBlockState(pos);
            pos = pos.offset(side);
            double yOffset = 0.0D;

            if (side == EnumFacing.UP && state.getBlock() instanceof BlockFence) {
                yOffset = 0.5D;
            }

            DinosaurEntity dinosaur = this.createDinosaurEntity(world, player, stack, pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D);

            if (dinosaur != null) {
                if (stack.hasDisplayName()) {
                    dinosaur.setCustomNameTag(stack.getDisplayName());
                }

                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }

                world.spawnEntity(dinosaur);
                dinosaur.playLivingSound();
            }

            return EnumActionResult.SUCCESS;
        }
    }

    public DinosaurEntity createDinosaurEntity(World world, EntityPlayer player, ItemStack stack, double x, double y, double z) {
        DinosaurEntity entity = this.dinosaur.createEntity(world);

        boolean male;
        switch (SpawnEggInfo.fromStack(stack).getState()) {
            case MALE: male = true; break;
            case FEMALE: male = false; break;
            default: male = player.getRNG().nextBoolean(); break;
        }

        GenderComponent gender = entity.getOrExcept(EntityComponentTypes.GENDER);
        gender.male = male;

        entity.setPosition(x, y, z);
        entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
        entity.rotationYawHead = entity.rotationYaw;
        entity.renderYawOffset = entity.rotationYaw;
        return entity;
    }

    @Data
    @AllArgsConstructor
    private static class SpawnEggInfo {
        Dinosaur dinosaur;
        SpawnEggState state;

        private static SpawnEggInfo fromStack(ItemStack stack) {
            NBTTagCompound nbt = stack.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("SpawnEggInfo");
            return new SpawnEggInfo(
                    ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("Dinosaur"))),
                    SpawnEggState.fromName(nbt.getString("State"))
            );
        }

        private NBTTagCompound serialize() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Dinosaur", this.dinosaur.getRegName().toString());
            nbt.setString("State", this.state.name().toLowerCase(Locale.ROOT));
            return nbt;
        }
    }

    private enum SpawnEggState {
        MALE, FEMALE, RANDOM;

        private SpawnEggState next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        private static SpawnEggState fromName(String name) {
            for (SpawnEggState spawnEggState : values()) {
                if(spawnEggState.name().equalsIgnoreCase(name)) {
                    return spawnEggState;
                }
            }
            return MALE;
        }
    }
}
