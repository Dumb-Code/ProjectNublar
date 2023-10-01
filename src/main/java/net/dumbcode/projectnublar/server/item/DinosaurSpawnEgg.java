package net.dumbcode.projectnublar.server.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Locale;

public class DinosaurSpawnEgg extends BasicDinosaurItem {


    public DinosaurSpawnEgg(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(dinosaur, translationKey, properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching()) {
            SpawnEggInfo info = SpawnEggInfo.fromStack(stack);
            info.setState(info.getState().next());
            if (world.isClientSide) {
                String modeString;
                switch (info.getState()) {
                    case MALE: modeString = "male"; break;
                    case FEMALE: modeString = "female"; break;
                    default: modeString = "random"; break;
                }
                player.displayClientMessage(new TranslationTextComponent(ProjectNublar.MODID + ".spawnegg.genderchange." + modeString), false);
            }
            stack.getOrCreateTagElement(ProjectNublar.MODID).put("SpawnEggInfo", info.serialize());
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            BlockState state = world.getBlockState(clickedPos);

            BlockPos blockpos;
            if (state.getCollisionShape(world, clickedPos).isEmpty()) {
                blockpos = clickedPos;
            } else {
                blockpos = clickedPos.relative(direction);
            }


            DinosaurEntity dinosaur = this.createDinosaurEntity(world, player, stack, blockpos.getX() + 0.5D, blockpos.getY() + 0.5, blockpos.getZ() + 0.5D);

            if (dinosaur != null) {
                if (stack.hasCustomHoverName()) {
                    dinosaur.setCustomName(stack.getDisplayName());
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                ((ServerWorld) world).addFreshEntityWithPassengers(dinosaur);
                dinosaur.playAmbientSound();
            }

        }
        return ActionResultType.SUCCESS;
    }

    public DinosaurEntity createDinosaurEntity(World world, PlayerEntity player, ItemStack stack, double x, double y, double z) {
        DinosaurEntity entity = this.dinosaur.createEntity(world);

        boolean male;
        switch (SpawnEggInfo.fromStack(stack).getState()) {
            case MALE: male = true; break;
            case FEMALE: male = false; break;
            default: male = player.getRandom().nextBoolean(); break;
        }

        entity.get(EntityComponentTypes.GENDER).ifPresent(comp -> comp.male = male);

        entity.setPos(x, y, z);
        entity.yRot = MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F);
        return entity;
    }

    @Data
    @AllArgsConstructor
    private static class SpawnEggInfo {
        Dinosaur dinosaur;
        SpawnEggState state;

        private static SpawnEggInfo fromStack(ItemStack stack) {
            CompoundNBT nbt = stack.getOrCreateTagElement(ProjectNublar.MODID).getCompound("SpawnEggInfo");
            return new SpawnEggInfo(
                DinosaurHandler.getRegistry().getValue(new ResourceLocation(nbt.getString("Dinosaur"))),
                SpawnEggState.fromName(nbt.getString("State"))
            );
        }

        private CompoundNBT serialize() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("Dinosaur", this.dinosaur.getRegName().toString());
            nbt.putString("State", this.state.name().toLowerCase(Locale.ROOT));
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
