package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.Set;

public interface ConnectableBlockEntity {
    void addConnection(Connection connection);

    Set<Connection> getConnections();

    VoxelShape getOrCreateCollision();

    default boolean removedByFenceRemovers() {
        return true;
    }
}
