package net.dumbcode.projectnublar.server.utils;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.joml.Matrix4f;

import java.util.Optional;

@UtilityClass
public class MachineUtils {

    //Returns -1 if there is no water amount
    //todo: abstract to any fluid, not just water
    public static int getWaterAmount(ItemStack stack) {
        Optional<IFluidHandlerItem> capability = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
        if(capability.isPresent()) {
            IFluidHandler fluidHandler = capability.get();
            int totalAmount = getWaterAmount(fluidHandler);
            if(totalAmount != 0) {
                return totalAmount;
            }
        }
        if(stack.getItem() == Items.POTION && PotionUtils.getPotion(stack) == Potions.WATER) {
            return FluidType.BUCKET_VOLUME / 3;
        }
        FluidStack fluid =  new FluidBucketWrapper(stack).getFluid();
        return !fluid.isEmpty() && fluid.getFluid() == Fluids.WATER ? fluid.getAmount() : -1;
    }

    private static int getWaterAmount(IFluidHandler fluidHandler) {
        int totalAmount = 0;
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack contents = fluidHandler.getFluidInTank(i);
            if(!contents.isEmpty() && contents.getFluid() == Fluids.WATER && contents.getAmount() > 0) {
                FluidStack drain = fluidHandler.drain(new FluidStack(Fluids.WATER, contents.getAmount()), IFluidHandler.FluidAction.SIMULATE);
                totalAmount += drain.getAmount();

            }
        }
        return totalAmount;
    }
    
    public static ItemStack fillTank(ItemStack stack, FluidTank tank) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(stack, tank, tank.getSpace(), null, false);
        if(result.isSuccess()) {
            return result.getResult();
        } else if(tank.getFluidAmount() < tank.getCapacity() && stack.getItem() == Items.POTION && PotionUtils.getPotion(stack) == Potions.WATER) {
            tank.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME / 3), IFluidHandler.FluidAction.EXECUTE);
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        return stack;
    }

    public static float getPlantMatter(ItemStack stack, Level world, BlockPos pos) {
        if(stack.getItem() == Items.AIR) {
            return 0;
        }
        Block block = Block.byItem(stack.getItem());
        if(block instanceof BushBlock) {
            BlockState state = block.defaultBlockState();
            VoxelShape shape = block.getShape(state, world, pos, CollisionContext.empty());
            float area = 0;
            for (AABB aabb : shape.toAabbs()) {
                area += (float) (aabb.getXsize() * aabb.getYsize() * aabb.getZsize());
            }
            return Math.round(area * 10) / 10F;
        } else if(block instanceof LeavesBlock) {
            return 4;
        } else if(block instanceof VineBlock) {
            return 2;
        }
        return 0;
    }

    public static double getBoneMatter(ItemStack stack) {
        if(stack.getItem() == Items.AIR) {
            return 0;
        }
        if(stack.getItem() == Items.BONE) {
            return 1D;
        } else if(stack.getItem() == Items.BONE_MEAL) {
            return 1/3D;
        } else if(stack.getItem() == Items.SKELETON_SKULL) {
            return 4D;
        } else if(stack.getItem() == Blocks.BONE_BLOCK.asItem()) {
            return 3D;
        }
        if(stack.is(Tags.Items.BONES)) {
            return 1D;
        }
        return 0;
    }

    public static double getSugarMatter(ItemStack stack) {
        if(stack.getItem() == Items.AIR) {
            return 0;
        }
        if(stack.getItem() == Items.SUGAR) {
            return 1;
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawTiledTexture(GuiGraphics stack, float left, float top, float right, float bottom, TextureAtlasSprite sprite) {
        drawTiledTexture(stack, left, top, right, bottom, sprite.contents().width(), sprite.contents().height(), sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawTiledTexture(GuiGraphics stack, float left, float top, float right, float bottom, int renderWidth, int renderHeight, float minU, float minV, float maxU, float maxV) {
        if(renderWidth == 0 || renderHeight == 0) {
            return;
        }

        Matrix4f pose = stack.pose().last().pose();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        float height = bottom - top;
        float width = right - left;

        float endFullHeight = height - (height % renderHeight);
        float endFullWidth = width - (width % renderWidth);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for(int renderH = 0; renderH < endFullHeight; renderH += renderHeight) {
            float currentH = bottom - renderH;
            for (int renderW = 0; renderW < endFullWidth; renderW+= renderWidth) {
                float currentW = left + renderW;
                buffer.vertex(pose, currentW, currentH - renderHeight, 0.0F).uv(minU, minV).endVertex();
                buffer.vertex(pose, currentW, currentH, 0.0F).uv(minU, maxV).endVertex();
                buffer.vertex(pose, currentW + renderWidth, currentH, 0.0F).uv(maxU, maxV).endVertex();
                buffer.vertex(pose, currentW + renderWidth, currentH - renderHeight, 0.0F).uv(maxU, minV).endVertex();
            }
        }


        float xLeftOver = width % renderWidth;
        float xStart = left + endFullWidth;
        float rightU = maxU - (maxU - minU) * ((renderWidth - xLeftOver) / (float)renderWidth);

        if(xLeftOver != 0) {
            for(int renderH = 0; renderH < endFullHeight; renderH += renderHeight) {
                float currentH = bottom - renderH;
                buffer.vertex(pose, xStart, currentH - renderHeight, 0.0F).uv(minU, minV).endVertex();
                buffer.vertex(pose, xStart, currentH, 0.0F).uv(minU, maxV).endVertex();
                buffer.vertex(pose, xStart + xLeftOver, currentH, 0.0F).uv(rightU, maxV).endVertex();
                buffer.vertex(pose, xStart + xLeftOver, currentH - renderHeight, 0.0F).uv(rightU, minV).endVertex();
            }
        }


        float yLeftOver = height % renderHeight;
        float yStart = bottom - endFullHeight;
        float leftV = minV + (maxV - minV) * ((renderHeight - yLeftOver)/ (float)renderHeight);

        if(yLeftOver != 0) {
            for (int renderW = 0; renderW < endFullWidth; renderW+= renderWidth) {
                float currentW = left + renderW;

                buffer.vertex(pose, currentW, yStart - yLeftOver, 0.0F).uv(minU, leftV).endVertex();
                buffer.vertex(pose, currentW, yStart, 0.0F).uv(minU, maxV).endVertex();
                buffer.vertex(pose, currentW + renderWidth, yStart, 0.0F).uv(maxU, maxV).endVertex();
                buffer.vertex(pose, currentW + renderWidth, yStart - yLeftOver, 0.0F).uv(maxU, leftV).endVertex();
            }
        }

        if(xLeftOver != 0 || yLeftOver != 0) {
            float endStartW = left + endFullWidth;
            float endStartH = bottom - endFullHeight;

            buffer.vertex(pose, endStartW, endStartH - yLeftOver, 0.0F).uv(minU, leftV).endVertex();
            buffer.vertex(pose, endStartW, endStartH, 0.0F).uv(minU, maxV).endVertex();
            buffer.vertex(pose, endStartW + xLeftOver, endStartH, 0.0F).uv(rightU, maxV).endVertex();
            buffer.vertex(pose, endStartW + xLeftOver, endStartH - yLeftOver, 0.0F).uv(rightU, leftV).endVertex();
        }

        tessellator.end();
    }

    //Gives the player an item without the fake item
    //Originally copied and modified from jeis CommandUtilServer
    public static void giveToInventory(Player player, ItemStack itemStack) {
        int count = itemStack.getCount();
        boolean addedToInventory = player.getInventory().add(itemStack);

        if (addedToInventory) {
            player.level().playSound(null,
                player.xo, player.yo, player.zo,
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                0.2F, (player.getRandom().nextFloat() * 1.4F + 1.0F) * 2.0F
            );
            player.getInventory().setChanged();
        }

        if (addedToInventory && itemStack.isEmpty()) {
            player.awardStat(Stats.ITEM_PICKED_UP.get(itemStack.getItem()), count);
        } else {
            player.awardStat(Stats.ITEM_DROPPED.get(itemStack.getItem()), count - itemStack.getCount());
            ItemEntity entityitem = player.drop(itemStack, false);
            if (entityitem != null) {
                entityitem.setNoPickUpDelay();
                entityitem.setThrower(player.getUUID());
            }
        }
    }

}
