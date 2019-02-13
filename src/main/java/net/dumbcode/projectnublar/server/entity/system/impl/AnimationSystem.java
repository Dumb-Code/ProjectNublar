package net.dumbcode.projectnublar.server.entity.system.impl;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.TabulaUtils;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationRunWrapper;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public enum AnimationSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];
    private AgeComponent[] ages = new AgeComponent[0];
    private AnimationComponent[] animations = new AnimationComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.DINOSAUR, EntityComponentTypes.ANIMATION, EntityComponentTypes.AGE);
        this.entities = family.getEntities();
        this.dinosaurs = family.populateBuffer(EntityComponentTypes.DINOSAUR, this.dinosaurs); //todo ew
        this.ages = family.populateBuffer(EntityComponentTypes.AGE, this.ages);
        this.animations = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update() {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            AnimationComponent animation = this.animations[i];
            ModelStage age = this.ages[i].stage;
            Dinosaur dinosaur = this.dinosaurs[i].dinosaur;
            if(!entity.world.isRemote) { //server side only. Client side is already handled
                if(animation.animationWrapper == null) {

                    if (!dinosaur.getSystemInfo().allAcceptedStages().contains(age)) {
                        age = dinosaur.getSystemInfo().defaultStage();
                    }

                    ResourceLocation regname = dinosaur.getRegName();
                    ResourceLocation modelName = new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + age.getName().toLowerCase(Locale.ROOT) + "/" + dinosaur.getModelProperties().getMainModelMap().get(age));

                    Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(modelName);

                    List<AnimationLayer> layers = Lists.newArrayList();
                    for (PoseHandler.AnimationLayerFactory<DinosaurEntity, ModelStage> factory : dinosaur.getSystemInfo().createFactories()) {
                        Map<String, AnimationRunWrapper.CubeWrapper> map = new HashMap<>();
                        layers.add(factory.createWrapper((DinosaurEntity) entity, dinosaur.getSystemInfo().getStageFromEntity((DinosaurEntity) entity), cubes.keySet(), cubes::get, s -> map.computeIfAbsent(s, o -> new AnimationRunWrapper.CubeWrapper(cubes.get(o))), dinosaur.getSystemInfo(), true));
                    }
                    animation.animationWrapper = new AnimationRunWrapper(this.entities[i], layers);
                }
            }
            if(animation.animationWrapper != null) {
                List<AnimationLayer> layers = animation.animationWrapper.getLayers();
                for (AnimationLayer layer : layers) {
                    layer.animate(entity.ticksExisted);
                }
                Matrix4d entityRotate = new Matrix4d();
                entityRotate.rotY(-Math.toRadians(entity.rotationYaw));

                Function<String, AnimationLayer.AnimatableCube> function = layers.get(0).getAnicubeRef();
                String cube = "tail4";
                AnimationLayer.AnimatableCube animatableCube = function.apply(cube);
                if(animatableCube != null) {
                    Vec3d partOrigin = animatableCube.getModelPos(new Vec3d(animatableCube.getRotationPointX()/ 16, -animatableCube.getRotationPointY() / 16, -animatableCube.getRotationPointZ() / 16));
                    Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y + 1.5, partOrigin.z);
                    rendererPos.scale(2.5);
                    entityRotate.transform(rendererPos);
                    ProjectNublar.spawnParticles(ParticleType.SPARKS, entity.world, rendererPos.x+entity.posX, rendererPos.y+entity.posY, rendererPos.z+entity.posZ, 0, 0, 0, 8);
                }
            }
        }
    }
}
