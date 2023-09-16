package net.dumbcode.projectnublar.server.runtimepack.generator;

import net.dumbcode.projectnublar.server.runtimepack.generator.api.RRPPreGenEvent;
import net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class ARRP {
  public static final Logger LOGGER = LogManager.getLogger("BRRP");
  private static List<Future<?>> futures;

  public void onPreLaunch() {
    LOGGER.info("BRRP data generation: PreLaunch");
    List<Future<?>> futures = new ArrayList<>();
    MinecraftForge.EVENT_BUS.post(new RRPPreGenEvent(), (listener, event) -> futures.add(RuntimeResourcePackImpl.EXECUTOR_SERVICE.submit(() -> listener.invoke(event))));
    ARRP.futures = futures;
  }

  public static void waitForPregen() throws ExecutionException, InterruptedException {
    if (futures != null) {
      for (Future<?> future : futures) {
        future.get();
      }
      futures = null;
    }
  }
}
