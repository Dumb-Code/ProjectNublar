package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid=ProjectNublar.MODID)
public class InterpValue implements INBTSerializable<CompoundNBT> {

    private static final List<InterpValue> INSTANCES = Lists.newArrayList();
    private static final List<InterpValue> MARKED_REMOVE = Lists.newArrayList();

    private final Supplier<Boolean> supplier;

    private double speed;
    private double target;
    private double current;
    private double previousCurrent;
    private boolean initilized;

    @Deprecated
    public InterpValue(double speed) {
        this(() -> true, speed);
    }

    public InterpValue(Entity entity, double speed) {
        this(entity::isAlive, speed);
    }

    public InterpValue(Supplier<Boolean> supplier, double speed) {
        this.speed = speed;
        this.supplier = supplier;
        INSTANCES.add(this);
    }

    public void setTarget(double target) {
        if(!initilized) {
            initilized = true;
            reset(target);
        } else {
            this.target = target;
        }
    }

    public void reset(double target) {
        this.previousCurrent = target;
        this.current = target;
        this.target = target;
    }

    private void tickInterp() {
        if(!supplier.get()) {
            MARKED_REMOVE.add(this);
            return;
        }
        this.previousCurrent = current;
        if(Math.abs(current - target) <= speed) {
            current = target;
        } else if(current < target) {
            current += speed;
        } else {
            current -= speed;
        }
    }

    public double getValueForRendering(float partialTicks) {
        return previousCurrent + (current - previousCurrent) * partialTicks;
    }

    public double getCurrent() {
        return current;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("target", target);
        tag.putDouble("current", current);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.target = nbt.getDouble("target");
        this.current = nbt.getDouble("current");
        this.previousCurrent = current;
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        Dist side = FMLEnvironment.dist;
        if((event instanceof TickEvent.ClientTickEvent && side.isClient()) || (event instanceof TickEvent.ServerTickEvent && side.isDedicatedServer())) {
            INSTANCES.forEach(InterpValue::tickInterp);
            MARKED_REMOVE.forEach(INSTANCES::remove);
            MARKED_REMOVE.clear();
        }
    }
}