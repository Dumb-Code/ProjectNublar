package net.dumbcode.projectnublar.server.containers;

import net.dumbcode.projectnublar.client.gui.GuiTrackingBeacon;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockTrackingBeacon;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.pouch.FossilPouchMenu;
import net.dumbcode.projectnublar.server.containers.pouch.FossilPouchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class ProjectNublarContainers {
    public static final DeferredRegister<ContainerType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, ProjectNublar.MODID);

    public static final RegistryObject<MenuType<MachineModuleContainer>> MACHINE_MODULES = REGISTER.register("machine_module",
        create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            int tab = data.readInt();

            TileEntity entity = inv.player.level.getBlockEntity(pos);
            if(entity instanceof MachineModuleBlockEntity) {
                MachineModuleBlockEntity<?> be = (MachineModuleBlockEntity<?>) entity;
                return be.createContainer(windowId, inv.player, tab);
            }
            String teClazz = entity == null ? "@null" : entity.getClass().getSimpleName();
            throw new IllegalStateException("Illegal point, tried to open machine at " + pos + " but found tileentity of " + teClazz);
        })
    );


    public static final RegistryObject<ContainerType/*MenuType*/<FossilPouchMenu>> SACK_MENU = registerMenuType(FossilPouchMenu::new, "sack_menu");

    public static final RegistryObject<ContainerType<BlockTrackingBeacon.TrackingContainer>> TRACKING_BEACON = REGISTER.register("tracking_beacon", create((windowId, inv, data) -> {
        BlockPos blockPos = data.readBlockPos();
        TileEntity entity = inv.player.level.getBlockEntity(blockPos);
        if(entity instanceof TrackingBeaconBlockEntity) {
            return new BlockTrackingBeacon.TrackingContainer(windowId, (TrackingBeaconBlockEntity) entity);
        }

        String teClazz = entity == null ? "@null" : entity.getClass().getSimpleName();
        throw new IllegalStateException("Illegal point, tried to open tracking beacon at " + blockPos + " but found tileentity of " + teClazz);
    }));

    private static <T extends Container> Supplier<ContainerType<T>> create(IContainerFactory<T> factory) {
        return () -> new ContainerType<>(factory);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerScreens() {
        //Doesn't compile without the <>
        ScreenManager.<MachineModuleContainer, TabbedGuiContainer<MachineModuleContainer>>register(MACHINE_MODULES.get(), (container, inventory, title) -> {
            MachineModuleBlockEntity<?> be = container.getBlockEntity();
            Screen screen = Minecraft.getInstance().screen;
            TabInformationBar bar;
            if(screen instanceof TabbedGuiContainer) {
                bar = ((TabbedGuiContainer<?>) screen).getInfo();
            } else {
                bar = be.createInfo();
            }
            return be.createScreen(container, inventory, title, bar, container.getTab());
        });
        ScreenManager.register(ProjectNublarContainers.SACK_MENU.get(), FossilPouchScreen::new);

        ScreenManager.<BlockTrackingBeacon.TrackingContainer, GuiTrackingBeacon>register(TRACKING_BEACON.get(), (container, inventory, title) -> new GuiTrackingBeacon(container));
    }

    public static <T extends Container> Optional<T> getFromMenu(Class<T> expected, @Nullable ServerPlayerEntity player) {
        if(player != null && expected.isInstance(player.containerMenu)) {
            return Optional.of(expected.cast(player.containerMenu));
        }
        return Optional.empty();
    }

    private static <T extends Container> RegistryObject<ContainerType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return REGISTER.register(name, () -> IForgeContainerType.create(factory));
    }
}
