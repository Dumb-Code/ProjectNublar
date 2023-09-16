package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.server.runtimepack.generator.api.RRPCallbackForge;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class ReloadableResourceManagerImplMixin {
  @Shadow
  @Final
  private ResourcePackType type;

  @ModifyVariable(method = "createFullReload",
      at = @At(value = "HEAD"),
      argsOnly = true)
  private List<IResourcePack> registerARRPs(List<IResourcePack> packs) throws ExecutionException, InterruptedException {
    List<IResourcePack> before = new ArrayList<>();
    RRPCallbackForge.BEFORE_VANILLA.build().stream().map(f -> f.apply(type)).filter(Objects::nonNull).forEach(before::add);
    before.addAll(packs);
    RRPCallbackForge.AFTER_VANILLA.build().stream().map(f -> f.apply(type)).filter(Objects::nonNull).forEach(before::add);
    return before;
  }
}