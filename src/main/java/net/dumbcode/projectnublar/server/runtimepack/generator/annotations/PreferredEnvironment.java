package net.dumbcode.projectnublar.server.runtimepack.generator.annotations;

import net.minecraftforge.api.distmarker.Dist;

import java.lang.annotation.*;

/**
 * <p>It indicates that the API has something that prefers to be annotated with {@link Dist} to exist in the specified environment. It itself is not annotated {@code @Environment} for compatibility and same rare exception uses, but it's highly recommended to, when overriding, annotate the overriding methods with {@code @Environment}.</p>
 * <p>Remember that this annotation is only a reminder. It does not take real effect.</p>
 *
 * @see Dist
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
@Documented
public @interface PreferredEnvironment {
  /**
   * Represents the environment that the annotated element is preferred to be only present in, and overriding methods are preferred to be annotated with <code>@Environment(<i>the value</i>)</code>.
   */
  Dist value();
}
