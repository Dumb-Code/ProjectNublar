package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.utils.Connection;

import java.util.Set;

public interface ConnectableBlockEntity {
    void addConnection(Connection connection);

    Set<Connection> getConnections();

    default boolean removedByFenceRemovers() {
        return true;
    }
}
