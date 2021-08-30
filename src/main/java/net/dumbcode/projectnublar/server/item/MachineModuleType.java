package net.dumbcode.projectnublar.server.item;

import lombok.Value;

@Value
public class MachineModuleType {


    //Upgrades:
    public static final MachineModuleType COMPUTER_CHIP = new MachineModuleType("computer");
    public static final MachineModuleType TANKS = new MachineModuleType("tanks");
    public static final MachineModuleType DRILL_BIT = new MachineModuleType("drill_bit");
    public static final MachineModuleType LEVELING_SENSORS = new MachineModuleType("leveling_sensors");
    public static final MachineModuleType BULB = new MachineModuleType("bulb");
    public static final MachineModuleType CONTAINER = new MachineModuleType("container");
    public static final MachineModuleType TURBINES = new MachineModuleType("turbines");

    private final String name;
}
