package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.stream.Collectors;

public class DinosaurEggItem extends DnaHoverDinosaurItem {


    public DinosaurEggItem(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(dinosaur, translationKey, properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = player.level;
        Vector3d location = context.getClickLocation();
        BlockPos pos = context.getClickedPos();
        if(!world.isClientSide) {
            DinosaurEntity entity = this.getDinosaur().createEntity(world);
            entity.get(EntityComponentTypes.GENDER).ifPresent(c -> c.male = world.random.nextBoolean());

            entity.get(EntityComponentTypes.GENETICS).ifPresent(genetics -> {
                GeneticComponent.replaceGenetics(genetics, entity,
                    context.getItemInHand().getOrCreateTagElement(ProjectNublar.MODID)
                        .getList("Genetics", Constants.NBT.TAG_COMPOUND).stream()
                        .map(g -> GeneticEntry.deserialize((CompoundNBT) g))
                        .collect(Collectors.toList())
                );

                GeneticComponent.mutateGenes(genetics, entity);
            });

            entity.setPos(pos.getX() + location.x, pos.getY() + location.y, pos.getZ() + location.z);
            entity.xRot = 0;
            entity.yRot = MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F);

            world.addFreshEntity(entity);
        }
        return ActionResultType.SUCCESS;
    }
}
