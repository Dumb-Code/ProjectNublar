package net.dumbcode.projectnublar.client.render.dinosaur;

import com.google.common.collect.Lists;
import net.ilexiconn.llibrary.server.animation.Animation;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum DinosaurAnimations {
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

    DinosaurAnimations(boolean hold, boolean blockMovement) {
        this(hold, blockMovement, true);
    }

    DinosaurAnimations(boolean hold, boolean blockMovement, boolean useInertia) {
        this.hold = hold;
        this.doesBlockMovement = blockMovement;
        this.useInertia = useInertia;
    }

    DinosaurAnimations() {
        this(false, true);
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
        for (DinosaurAnimations dinosaurAnimations : values()) {
            list.add(dinosaurAnimations.name().toLowerCase(Locale.ROOT));
        }
        return list;
    }

    public static DinosaurAnimations getAnimation(Animation animation) {
        for (DinosaurAnimations animations : values()) {
            if (animation.equals(animations.animation)) {
                return animations;
            }
        }

        return DinosaurAnimations.IDLE;
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
