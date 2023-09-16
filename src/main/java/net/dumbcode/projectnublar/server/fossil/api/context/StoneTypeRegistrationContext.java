package net.dumbcode.projectnublar.server.fossil.api.context;

import com.google.common.base.Stopwatch;
import net.dumbcode.projectnublar.server.fossil.Fossils;
import net.dumbcode.projectnublar.server.fossil.api.FossilExtensionManager;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;

public class StoneTypeRegistrationContext {
    /**
     * Registers a stone type
     * @param type the stone type to register
     */
    public void registerStoneType(StoneType type) {
        FossilExtensionManager.LOGGER.info("Registering Stone Type {}", type.name);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Fossils.STONE_TYPES.add(type);
        FossilExtensionManager.LOGGER.info("Registered Stone Type {}, \033[0;31mTook {}\033[0;0m", type.name, stopwatch);
    }

    /**
     * Turns a rgba color into an int passed in to {@link StoneType#tint}
     * @param pAlpha the alpha component
     * @param pRed the red color
     * @param pGreen the green color
     * @param pBlue the blue color
     * @return an int passed in to {@link StoneType#tint}
     */
    public int color(int pAlpha, int pRed, int pGreen, int pBlue) {
        return pAlpha << 24 | pRed << 16 | pGreen << 8 | pBlue;
    }
}