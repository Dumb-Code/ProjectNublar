package net.dumbcode.projectnublar.server.entity.vehicles;

import lombok.Value;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.utils.InterpValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

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
    public boolean canBePushed() {
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    public boolean canBeCollidedWith() {
        if(this.world.isRemote) {
            return !this.isPlayerIn();
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    private boolean isPlayerIn() {
        return this.getPassengers().contains(Minecraft.getMinecraft().player);
    }

    @Override
    protected void applyMovement() {
        Entity driver = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0); //Move to controlling passanger
        if(driver != null) {
            this.rotationYaw = driver.rotationYaw;
        }

        float velX = (float) this.getIntInput(DefaultInput.LEFT) - this.getIntInput(DefaultInput.RIGHT);
        float velZ = (float) this.getIntInput(DefaultInput.FORWARD) - this.getIntInput(DefaultInput.BACKWARDS);

        velX /= 30F;
        velZ /= 30F;

        double sin = Math.sin(Math.toRadians(this.rotationYaw));
        double cos = Math.cos(Math.toRadians(this.rotationYaw));

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

    private void runClientMovement() {
        if(this.rotation == null) {
            this.rotation = new Quaternion(1, 0, 0, 0);
            this.prevRotation = new Quaternion(1, 0, 0, 0);
        }
        Quaternion quatX = Vector3f.XP.rotation((float) (this.getDeltaMovement().z * this.getBbWidth() * Math.PI * 0.017453292F));
        Quaternion quatZ = Vector3f.ZP.rotation((float) (-this.getDeltaMovement().x * this.getBbWidth() * Math.PI * 0.017453292F));
        this.prevRotation = this.rotation;

        this.rotation = new Quaternion(quatX);
        this.rotation.mul(quatZ);
    }

    @Override
    protected boolean isActive(EntityPlayerSP player, DefaultInput defaultInput) {
        MovementInput move = player.movementInput;
        switch (defaultInput) {
            case LEFT: return move.leftKeyDown;
            case RIGHT: return move.rightKeyDown;
            case FORWARD: return move.forwardKeyDown;
            case BACKWARDS: return move.backKeyDown;
        }
        return false;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        player.startRiding(this);
        return true;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            passenger.setPosition(this.posX, this.posY + this.height / 2 - passenger.height / 2, this.posZ);
        }
    }
}
