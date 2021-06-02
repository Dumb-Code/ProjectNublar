package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Set;

public class ProjectNublarModelData {
    public static final ModelProperty<Set<Connection.CompiledRenderData>> CONNECTIONS = new ModelProperty<>();
    public static final ModelProperty<Double> FENCE_POLE_ROTATION_DEGS = new ModelProperty<>();
}
