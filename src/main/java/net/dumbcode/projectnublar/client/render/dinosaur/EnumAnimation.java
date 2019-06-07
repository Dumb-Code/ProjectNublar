package net.dumbcode.projectnublar.client.render.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum EnumAnimation  {
    IDLE(false, false),
    WALKING(false, false);

    private final Animation animation;

    EnumAnimation() {
        this(false, true);
    }

    EnumAnimation(boolean hold) {
        this(hold, true);
    }

    EnumAnimation(boolean hold, boolean useInertia) {
        this.animation = new Animation(hold, useInertia, this.name().toLowerCase());
    }

    public Animation get() {
        return this.animation;
    }

    public static Collection<String> getNames() {
        List<String> list = Lists.newArrayList();
        for (EnumAnimation animation : values()) {
            list.add(animation.name().toLowerCase(Locale.ROOT));
        }
        return list;
    }

    public static Animation fromName(String name) {
        EnumAnimation animation = IDLE;
        for (EnumAnimation animations : values()) {
            if (animations.name().equalsIgnoreCase(name)) {
                animation = animations;
            }
        }
        return animation.get();
    }

}
