package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.utils.Connection;

public interface ConnectableBlockEntity {
    void addConnection(Connection connection);
}
