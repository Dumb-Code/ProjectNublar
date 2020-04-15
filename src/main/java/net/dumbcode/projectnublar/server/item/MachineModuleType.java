package net.dumbcode.projectnublar.server.item;

import lombok.Value;

@Value
public class MachineModuleType {

    public static final MachineModuleType TEST = new MachineModuleType("test1");

    public static final MachineModuleType COMPUTER_CHIP = new MachineModuleType("computer");
    public static final MachineModuleType TANKS = new MachineModuleType("monitor");

    private final String name;
}
