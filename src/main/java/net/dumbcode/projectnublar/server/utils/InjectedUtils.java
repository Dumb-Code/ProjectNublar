package net.dumbcode.projectnublar.server.utils;

public class InjectedUtils {
    private static final Object NULL = null;

    @SuppressWarnings("unchecked")
    public static <T> T injected() {
        return (T) NULL;
    }
}
