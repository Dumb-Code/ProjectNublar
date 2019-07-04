package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class AnimationFactorHandler {

    private AnimationFactorHandler() {}

    public static final AnimationFactor LIMB_SWING = InjectedUtils.injected();

    @SubscribeEvent
    public static void onAnimationFactorRegister(RegistryEvent.Register<AnimationFactor> event) {
        event.getRegistry().registerAll(
                new AnimationFactor((access, partialTicks) -> {
                    EntityLivingBase entity = (EntityLivingBase) access; //don't use ELB
                    return (entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks) * 1.75F;
                    //TODO: look at RenderLivingBase L115 and just copy that with some multiplier
                }).setRegistryName("limb_swing")
        );
    }
}
