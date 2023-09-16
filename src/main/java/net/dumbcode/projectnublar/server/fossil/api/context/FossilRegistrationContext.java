package net.dumbcode.projectnublar.server.fossil.api.context;

import com.google.common.base.Stopwatch;
import net.dumbcode.projectnublar.server.fossil.Fossils;
import net.dumbcode.projectnublar.server.fossil.api.FossilExtensionManager;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;

public class FossilRegistrationContext {
    /**
     * Registers a fossil
     * @param fossil the fossil to register
     */
    public void registerFossil(Fossil fossil) {
        FossilExtensionManager.LOGGER.info("Registering Fossil {}", fossil.name);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Fossils.FOSSILS.add(fossil);
        FossilExtensionManager.LOGGER.info("Registered Fossil {}, \033[0;31mTook {}\033[0;0m", fossil.name, stopwatch);
    }
}