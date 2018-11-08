package net.dumbcode.projectnublar.server.utils;

import lombok.experimental.UtilityClass;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@UtilityClass
public class MachineUtils {

    //Returns -1 if there is no water amount
    //todo: abstract to any fluid, not just water
    public static int getWaterAmount(ItemStack stack) {
        if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,  null);
            if(fluidHandler != null) {
                int totalAmount = -1;
                for (IFluidTankProperties properties : fluidHandler.getTankProperties()) {
                    FluidStack contents = properties.getContents();
                    if(contents != null && contents.getFluid() == FluidRegistry.WATER && contents.amount > 0 && properties.canDrainFluidType(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME))) {
                        totalAmount += properties.getContents().amount;
                    }
                }
                if(totalAmount != -1) {
                    return totalAmount;
                }
            }
        }
        if(stack.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER) {
            return Fluid.BUCKET_VOLUME / 3;
        }
        FluidStack fluid =  new FluidBucketWrapper(stack).getFluid();
        return fluid != null && fluid.getFluid() == FluidRegistry.WATER ? fluid.amount : -1;
    }
    
    public static ItemStack fillTank(ItemStack stack, FluidTank tank) {
        if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,  null);
            if(fluidHandler != null) {
                FluidStack fluidStack = new FluidStack(FluidRegistry.WATER, tank.getCapacity() - tank.getFluidAmount());
                FluidStack drained = fluidHandler.drain(fluidStack, false);
                if(drained != null && tank.fill(drained, false) > 0) {
                    tank.fill(fluidHandler.drain(fluidStack, true), true);
                }
            }
        } else if(tank.getFluidAmount() < tank.getCapacity()) {
            if(stack.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER) {
                tank.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME / 3), true);
                return new ItemStack(Items.GLASS_BOTTLE);
            } else {
                int waterAmount = getWaterAmount(stack);
                if(waterAmount != -1) {
                    FluidBucketWrapper wrapper = new FluidBucketWrapper(stack);
                    FluidStack drained = wrapper.drain(waterAmount, true);
                    if(drained != null) {
                        tank.fill(drained, true);
                    } else {
                        ProjectNublar.getLogger().warn("Tried to drain item {}, but yielded no results", stack.getItem().getRegistryName());
                    }
                    return wrapper.getContainer();
                }
            }
        }
        return stack;
    }

    public static double getPlantMatter(ItemStack stack, World world, BlockPos pos) {
        if(stack.getItem() == Items.AIR) {
            return 0;
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        if(block instanceof BlockBush) {
            try {
                IBlockState state = block.getDefaultState();
                if(stack.getItem() instanceof ItemBlock) {
                    state = block.getStateFromMeta(stack.getItem().getMetadata(stack.getItemDamage()));
                }
                AxisAlignedBB aabb = block.getSelectedBoundingBox(state, world, pos);
                return (aabb.maxX - aabb.minX) * (aabb.maxY - aabb.minY) * (aabb.maxZ - aabb.minZ);
            } catch (Exception e) {
                return 2;
            }
        } else if(block instanceof BlockLeaves) {
            return 4;
        } else if(block instanceof BlockVine) {
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
        } else if(stack.getItem() == Items.DYE && stack.getMetadata() == EnumDyeColor.WHITE.getDyeDamage()) {
            return 1/3D;
        } else if(stack.getItem() == Items.SKULL && stack.getMetadata() == 0) {
            return 4D;
        } else if(stack.getItem() == Item.getItemFromBlock(Blocks.BONE_BLOCK)) {
            return 3D;
        }
        return 0;
    }

    public static double getSugarMatter(ItemStack stack) {
        if(stack.getItem() == Items.AIR) {
            return 0;
        }
        if(stack.getItem() == Items.SUGAR || stack.getItem() == Items.REEDS) {
            return 1;
        }
        return 0;
    }


    @SideOnly(Side.CLIENT)
    public static void drawTiledTexture(float left, float top, float right, float bottom, int renderWidth, int renderHeight, float minU, float minV, float maxU, float maxV) {
        if(renderWidth == 0 || renderHeight == 0) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float height = bottom - top;
        float width = right - left;

        float endFullHeight = height - (height % renderHeight);
        float endFullWidth = width - (width % renderWidth);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for(int renderH = 0; renderH < endFullHeight; renderH += renderHeight) {
            float currentH = bottom - renderH;
            for (int renderW = 0; renderW < endFullWidth; renderW+= renderWidth) {
                float currentW = left + renderW;
                buffer.pos(currentW, currentH - renderHeight, 0.0D).tex(minU, minV).endVertex();
                buffer.pos(currentW, currentH, 0.0D).tex(minU, maxV).endVertex();
                buffer.pos(currentW + renderWidth, currentH, 0.0D).tex(maxU, maxV).endVertex();
                buffer.pos(currentW + renderWidth, currentH - renderHeight, 0.0D).tex(maxU, minV).endVertex();
            }
        }


        float xLeftOver = width % renderWidth;
        float xStart = left + endFullWidth;
        float rightU = maxU - (maxU - minU) * ((renderWidth - xLeftOver) / (float)renderWidth);

        if(xLeftOver != 0) {
            for(int renderH = 0; renderH < endFullHeight; renderH += renderHeight) {
                float currentH = bottom - renderH;
                buffer.pos(xStart, currentH - renderHeight, 0.0D).tex(minU, minV).endVertex();
                buffer.pos(xStart, currentH, 0.0D).tex(minU, maxV).endVertex();
                buffer.pos(xStart + xLeftOver, currentH, 0.0D).tex(rightU, maxV).endVertex();
                buffer.pos(xStart + xLeftOver, currentH - renderHeight, 0.0D).tex(rightU, minV).endVertex();
            }
        }


        float yLeftOver = height % renderHeight;
        float yStart = bottom - endFullHeight;
        float leftV = minV + (maxV - minV) * ((renderHeight - yLeftOver)/ (float)renderHeight);

        if(yLeftOver != 0) {
            for (int renderW = 0; renderW < endFullWidth; renderW+= renderWidth) {
                float currentW = left + renderW;

                buffer.pos(currentW, yStart - yLeftOver, 0.0D).tex(minU, leftV).endVertex();
                buffer.pos(currentW, yStart, 0.0D).tex(minU, maxV).endVertex();
                buffer.pos(currentW + renderWidth, yStart, 0.0D).tex(maxU, maxV).endVertex();
                buffer.pos(currentW + renderWidth, yStart - yLeftOver, 0.0D).tex(maxU, leftV).endVertex();
            }
        }

        if(xLeftOver != 0 || yLeftOver != 0) {
            float endStartW = left + endFullWidth;
            float endStartH = bottom - endFullHeight;

            buffer.pos(endStartW, endStartH - yLeftOver, 0.0D).tex(minU, leftV).endVertex();
            buffer.pos(endStartW, endStartH, 0.0D).tex(minU, maxV).endVertex();
            buffer.pos(endStartW + xLeftOver, endStartH, 0.0D).tex(rightU, maxV).endVertex();
            buffer.pos(endStartW + xLeftOver, endStartH - yLeftOver, 0.0D).tex(rightU, leftV).endVertex();
        }

        tessellator.draw();
    }

}
