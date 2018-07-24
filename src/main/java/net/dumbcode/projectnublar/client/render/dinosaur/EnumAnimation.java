package net.dumbcode.projectnublar.client.render.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.animation.AnimationInfo;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.server.animation.Animation;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum EnumAnimation implements AnimationInfo {
    IDLE(false, false, false),
    ATTACKING(false, false),
    INJURED(false, false),
    HEAD_COCKING,
    CALLING,
    HISSING,
    POUNCING(false, false),
    SNIFFING,
    EATING,
    DRINKING,
    MATING(false, false),
    SLEEPING(true, false),
    RESTING(true, true),
    ROARING,
    SPEAK(false, false),
    LOOKING_LEFT,
    LOOKING_RIGHT,
    BEGGING,
    SNAP,
    DYING(true, false, false),
    SCRATCHING,
    SPITTING,
    PECKING,
    PREENING,
    TAIL_DISPLAY,
    REARING_UP,
    LAYING_EGG,
    GIVING_BIRTH,
    GLIDING(true, false, true),
    ON_LAND(false, true, false),
    WALKING(false, false, false), RUNNING(false, false, false), SWIMMING(false, false, false), FLYING(false, false, false), CLIMBING(false, false, false),
    PREPARE_LEAP(false, false), LEAP(true, false), LEAP_LAND(true, false, false),
    START_CLIMBING(false, false),
    DILOPHOSAURUS_SPIT(false, false);


    private Animation animation;
    private boolean hold;
    private boolean doesBlockMovement;
    private boolean useInertia;

    EnumAnimation() {
        this(false, true);
    }

    EnumAnimation(boolean hold, boolean blockMovement) {
        this(hold, blockMovement, true);
    }

    EnumAnimation(boolean hold, boolean blockMovement, boolean useInertia) {
        this.hold = hold;
        this.doesBlockMovement = blockMovement;
        this.useInertia = useInertia;
    }



    public static Animation[] getAnimations() {
        Animation[] animations = new Animation[values().length];

        for (int i = 0; i < animations.length; i++) {
            animations[i] = values()[i].get();
        }

        return animations;
    }

    public static Collection<String> getNames() {
        List<String> list = Lists.newArrayList();
        for (EnumAnimation animation : values()) {
            list.add(animation.name().toLowerCase(Locale.ROOT));
        }
        return list;
    }

    public static EnumAnimation getAnimation(Animation animation) {
        for (EnumAnimation animations : values()) {
            if (animation.equals(animations.animation)) {
                return animations;
            }
        }
        return EnumAnimation.IDLE;
    }

    public static Animation fromName(String name) {
        EnumAnimation animation = IDLE;
        for (EnumAnimation animations : values()) {
            if(animations.name().equalsIgnoreCase(name)) {
                animation = animations;
            }
        }
        return animation.get();
    }

    public Animation get() {
        if (this.animation == null) {
            this.animation = Animation.create(-1);
        }
        return this.animation;
    }

    public boolean shouldHold() {
        return this.hold;
    }

    public boolean doesBlockMovement() {
        return this.doesBlockMovement;
    }

    public boolean useInertia() {
        return this.useInertia;
    }
}
