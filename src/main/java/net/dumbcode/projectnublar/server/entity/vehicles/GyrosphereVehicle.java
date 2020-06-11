package net.dumbcode.projectnublar.server.entity.vehicles;

import lombok.Value;
import net.dumbcode.projectnublar.server.utils.InterpValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

public class GyrosphereVehicle extends AbstractVehicle<AbstractVehicle.DefaultInput> {

    private final InterpValue xMotion = new InterpValue(this, 0.1D);
    private final InterpValue zMotion = new InterpValue(this, 0.1D);

    @SideOnly(Side.CLIENT)
    public Quaternion rotation;
    @SideOnly(Side.CLIENT)
    public Quaternion prevRotation;

    public GyrosphereVehicle(World worldIn) {
        super(worldIn, DefaultInput.values());
        this.setSize(4, 4);
        this.stepHeight = this.height/2;
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

        this.motionX += this.xMotion.getCurrent();
        this.motionZ += this.zMotion.getCurrent();

        float friction = this.inWater ? 0.8F : 0.95F;
        this.motionX *= friction;
        this.motionZ *= friction;

        this.motionY -= this.inWater ? 0.01F : 0.15F; //TODO: check no gravity

        if(this.world.isRemote) {
            this.runClientMovement();
        }

    }

    @SideOnly(Side.CLIENT)
    private void runClientMovement() {
        if(this.rotation == null) {
            this.rotation = new Quaternion(1, 0, 0, 0);
            this.prevRotation = new Quaternion(1, 0, 0, 0);
        }
        Quaternion quatX = new Quaternion();
        quatX.setFromAxisAngle(new Vector4f(1f, 0f, 0f, (float) (this.motionZ * this.width * Math.PI * 0.017453292F)));
        quatX.normalise();
        Quaternion quatZ = new Quaternion();
        quatZ.setFromAxisAngle(new Vector4f(0f, 0f, 1f, (float) (-this.motionX * this.width * Math.PI * 0.017453292F)));
        quatZ.normalise();
        this.prevRotation = this.rotation;
        this.rotation = Quaternion.mul(Quaternion.mul(quatX, quatZ, null), this.rotation, null);
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
