package net.dumbcode.projectnublar.server.registry;

import lombok.RequiredArgsConstructor;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

//An abstraction of the deferred register used to handle stuff that need to go before blocks and items.
public class EarlyDeferredRegister<T extends IForgeRegistryEntry<T>> {
    private final DeferredRegister<T> register;

    public static <T extends IForgeRegistryEntry<T>> EarlyDeferredRegister<T> wrap(DeferredRegister<T> register) {
        return new EarlyDeferredRegister<T>(register);
    }

    private EarlyDeferredRegister(DeferredRegister<T> register) {
        this.register = register;
    }

    public Collection<RegistryObject<T>> getEntries() {
        return this.register.getEntries();
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> sup) {
        return this.register.register(name, sup);
    }

    public Supplier<IForgeRegistry<T>> makeRegistry(String name, Supplier<RegistryBuilder<T>> sup) {
        return this.register.makeRegistry(name, sup);
    }


    public void register(IEventBus bus) {
        this.register.register(new EventBusDelegate(bus));
    }

    @RequiredArgsConstructor
    public static class DelegateEventDispatcher {
        private final DeferredRegister.EventDispatcher delegate;

        @SubscribeEvent
        public void onEarlyEvent(EarlyRegistryEvent<?> event) {
            this.delegate.handleEvent(event.asEvent());
        }
    }


    @RequiredArgsConstructor
    private static class EventBusDelegate implements IEventBus {
        private final IEventBus bus;

        @Override
        public void register(Object target) {
            if (target instanceof DeferredRegister.EventDispatcher) {
                this.bus.register(new DelegateEventDispatcher((DeferredRegister.EventDispatcher) target));
            } else {
                this.bus.register(target);
            }
        }

        //Delegates as @Delegate throws a weird error.
        public <T extends Event> void addListener(Consumer<T> consumer) {
            this.bus.addListener(consumer);
        }

        public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
            this.bus.addListener(priority, consumer);
        }

        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
            this.bus.addListener(priority, receiveCancelled, consumer);
        }

        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {
            this.bus.addListener(priority, receiveCancelled, eventType, consumer);
        }

        public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, Consumer<T> consumer) {
            this.bus.addGenericListener(genericClassFilter, consumer);
        }

        public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, Consumer<T> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, consumer);
        }

        public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, receiveCancelled, consumer);
        }

        public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, receiveCancelled, eventType, consumer);
        }

        public void unregister(Object object) {
            this.bus.unregister(object);
        }

        public boolean post(Event event) {
            return this.bus.post(event);
        }

        public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
            return this.bus.post(event, wrapper);
        }

        public void shutdown() {
            this.bus.shutdown();
        }

        public void start() {
            this.bus.start();
        }
    }
}
