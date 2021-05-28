package net.dumbcode.projectnublar.server.containers;

import net.dumbcode.projectnublar.client.gui.machines.CoalGeneratorGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.CoalGeneratorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.CoalGeneratorContainer;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectNublarContainers {
    public static final DeferredRegister<ContainerType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, ProjectNublar.MODID);

    public static final RegistryObject<ContainerType<CoalGeneratorContainer>> COAL_GENERATOR
        = REGISTER.register("coal_generator", () -> new ContainerType<>(CoalGeneratorContainer::new));

    public static void registerScreens() {
        ScreenManager.register(COAL_GENERATOR.get(), new ScreenManager.IScreenFactory<CoalGeneratorContainer, Screen>() {
            @Override
            public Screen create(CoalGeneratorContainer p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
                return
            }
        });
    }
}
