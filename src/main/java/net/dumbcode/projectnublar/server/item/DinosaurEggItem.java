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
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.Mth;
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
        CompoundNBT nbt = context.getItemInHand().getOrCreateTagElement(ProjectNublar.MODID);
        if(!world.isClientSide) {
            DinosaurEntity entity = this.getDinosaur().createEntity(world,
                this.getDinosaur().getAttacher().getDefaultConfig()
                    .runBeforeFinalize(EntityComponentTypes.GENETICS.get(), genetics -> {
                        genetics.disableRandomGenetics();
                        nbt.getList("Genetics", Constants.NBT.TAG_COMPOUND).stream()
                            .map(g -> GeneticEntry.deserialize((CompoundNBT) g))
                            .forEach(genetics::insertGenetic);
                    })
                    .runBeforeFinalize(EntityComponentTypes.GENDER.get(), gender -> {
                        if(nbt.contains("IsMale", Constants.NBT.TAG_BYTE)) {
                            gender.male = nbt.getBoolean("IsMale");
                        } else {
                            gender.male = world.random.nextBoolean();
                        }
                    })
            );
            entity.get(EntityComponentTypes.GENETICS.get()).ifPresent(genetics -> GeneticComponent.mutateGenes(genetics, entity));
            entity.setPos(location.x, location.y, location.z);
            entity.xRot = 0;
            entity.yRot = Mth.wrapDegrees(world.random.nextFloat() * 360.0F);

            world.addFreshEntity(entity);
        }
        return ActionResultType.SUCCESS;
    }
}
