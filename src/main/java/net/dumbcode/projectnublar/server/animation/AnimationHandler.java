package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class AnimationHandler {

    public static final Animation WALKING = new Animation(ProjectNublar.MODID, "walking");
    public static final Animation ATTACK = new Animation(ProjectNublar.MODID, "attack");
    public static final Animation CALL_SHORT = new Animation(ProjectNublar.MODID, "call_short");
    public static final Animation CALLING = new Animation(ProjectNublar.MODID, "calling");
    public static final Animation DRINKING = new Animation(ProjectNublar.MODID, "drinking");
    public static final Animation EATING = new Animation(ProjectNublar.MODID, "eating");
    public static final Animation INJURED = new Animation(ProjectNublar.MODID, "injured");
    public static final Animation LOOK_AROUND = new Animation(ProjectNublar.MODID, "look_around");
    public static final Animation RESTING = new Animation(ProjectNublar.MODID, "resting");
    public static final Animation ROARING = new Animation(ProjectNublar.MODID, "roaring");
    public static final Animation RUNNING = new Animation(ProjectNublar.MODID, "running");
    public static final Animation SNIFF_AIR = new Animation(ProjectNublar.MODID, "sniff_air");
    public static final Animation SNIFF_GROUND = new Animation(ProjectNublar.MODID, "sniff_groun");
}
