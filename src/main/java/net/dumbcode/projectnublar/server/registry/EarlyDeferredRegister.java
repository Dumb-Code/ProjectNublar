package net.dumbcode.projectnublar.server.registry;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collection;
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
        @Delegate(excludes = Excludes.class)
        private final IEventBus bus;

        @Override
        public void register(Object target) {
            if (target instanceof DeferredRegister.EventDispatcher) {
                this.bus.register(new DelegateEventDispatcher((DeferredRegister.EventDispatcher) target));
            } else {
                this.bus.register(target);
            }
        }
    }



    private interface Excludes {
        void register(Object target);
    }
}
