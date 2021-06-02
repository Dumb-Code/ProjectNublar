package net.dumbcode.projectnublar.server.containers;

import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ProjectNublarContainers {
    public static final DeferredRegister<ContainerType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, ProjectNublar.MODID);

    public static final RegistryObject<ContainerType<MachineModuleContainer>> MACHINE_MODULES = REGISTER.register("machine_module",
        create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            int tab = data.readInt();

            TileEntity entity = inv.player.level.getBlockEntity(pos);
            if(entity instanceof MachineModuleBlockEntity) {
                MachineModuleBlockEntity<?> be = (MachineModuleBlockEntity<?>) entity;
                return be.createContainer(windowId, inv.player, tab);
            }
            throw new IllegalStateException("Illegal point, tried to open machine at " + pos + " but found tileentity of " + entity.getClass().getSimpleName());
        })
    );

    private static <T extends Container> Supplier<ContainerType<T>> create(IContainerFactory<T> factory) {
        return () -> new ContainerType<>(factory);
    }

    public static void registerScreens() {
        ScreenManager.register(MACHINE_MODULES.get(), (container, inventory, title) -> {
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
    }
}
