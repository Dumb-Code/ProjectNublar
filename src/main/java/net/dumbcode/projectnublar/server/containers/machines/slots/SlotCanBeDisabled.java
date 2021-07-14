package net.dumbcode.projectnublar.server.containers.machines.slots;

public interface SlotCanBeDisabled {
    void setActive(boolean active);
    boolean isActive();
}
