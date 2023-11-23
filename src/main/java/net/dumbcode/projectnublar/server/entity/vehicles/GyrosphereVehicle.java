package net.dumbcode.projectnublar.server.entity.vehicles;

import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.utils.InterpValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class GyrosphereVehicle extends AbstractVehicle<AbstractVehicle.DefaultInput> {

    private final InterpValue xMotion = new InterpValue(this, 0.1D);
    private final InterpValue zMotion = new InterpValue(this, 0.1D);

    public Quaternion rotation;
    public Quaternion prevRotation;

    public GyrosphereVehicle(World worldIn) {
        this(EntityHandler.GYROSPHERE.get(), worldIn);
    }
    public GyrosphereVehicle(EntityType<?> type, World worldIn) {
        super(type, worldIn, DefaultInput.values());
        this.maxUpStep = this.getType().getHeight() / 2;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //    @Override
//    public boolean canBePushed() {
//        return true;
//    }


    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

//    @Override
//    public boolean canBeCollidedWith() {
//        if(this.level.isClientSide) {
//            return !this.isPlayerIn();
//        }
//        return true;
//    }

    private boolean isPlayerIn() {
        return this.getPassengers().contains(Minecraft.getInstance().player);
    }

    @Override
    protected void applyMovement() {
        Entity driver = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0); //Move to controlling passanger
        if(driver != null) {
            this.yRot = driver.yRot;
        }

        float velX = (float) this.getIntInput(DefaultInput.LEFT) - this.getIntInput(DefaultInput.RIGHT);
        float velZ = (float) this.getIntInput(DefaultInput.FORWARD) - this.getIntInput(DefaultInput.BACKWARDS);

        velX /= 30F;
        velZ /= 30F;

        double sin = Math.sin(Math.toRadians(this.yRot));
        double cos = Math.cos(Math.toRadians(this.yRot));

        this.xMotion.setTarget(velX * cos - velZ * sin);
        this.zMotion.setTarget(velX * sin + velZ * cos);

        Vector3d movement = this.getDeltaMovement();
        double motionX = movement.x;
        double motionY = movement.y;
        double motionZ = movement.z;
        motionX += this.xMotion.getCurrent();
        motionZ += this.zMotion.getCurrent();

        float friction = this.isInWater() ? 0.8F : 0.95F;
        motionX *= friction;
        motionZ *= friction;

        motionY -= this.isInWater() ? 0.01F : 0.15F; //TODO: check no gravity

        this.setDeltaMovement(motionX, motionY, motionZ);
        if(this.level.isClientSide) {
            this.runClientMovement();
        }

    }

    @Override
    public void push(double x, double y, double z) {
        super.push(x * 0.25F, y * 0.25F, z * 0.25F);
    }

    private void runClientMovement() {
        if(this.rotation == null) {
            this.rotation = new Quaternion(1, 0, 0, 0);
            this.prevRotation = new Quaternion(1, 0, 0, 0);
        }
        Quaternion quatX = Vector3f.XP.rotation((float) (this.getDeltaMovement().z * this.getBbWidth() * Math.PI * 0.017453292F));
        Quaternion quatZ = Vector3f.ZP.rotation((float) (-this.getDeltaMovement().x * this.getBbWidth() * Math.PI * 0.017453292F));
        this.prevRotation = this.rotation;

        Quaternion quaternion = new Quaternion(quatX);
        quaternion.mul(quatZ);
        quaternion.mul(this.rotation);

        this.rotation = quaternion;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected boolean isActive(ClientPlayerEntity player, DefaultInput defaultInput) {
        MovementInput move = player.input;
        switch (defaultInput) {
            case LEFT: return move.left;
            case RIGHT: return move.right;
            case FORWARD: return move.up;
            case BACKWARDS: return move.down;
        }
        return false;
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        player.startRiding(this);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void positionRider(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            passenger.setPos(this.position().x, this.position().y + this.getDimensions(Pose.STANDING).height / 2 - passenger.getDimensions(passenger.getPose()).height / 2, this.position().z);
        }
    }
}
