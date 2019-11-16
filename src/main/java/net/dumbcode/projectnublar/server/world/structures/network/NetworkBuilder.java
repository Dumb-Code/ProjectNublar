package net.dumbcode.projectnublar.server.world.structures.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.constants.ConstantDefinition;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.StructurePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;

import java.util.*;
import java.util.function.Function;

public class NetworkBuilder {

    private List<DataHandler> data = Lists.newArrayList();
    private Map<String, List<StructurePredicate>> globalPredicates = Maps.newHashMap();
    private StructureConstants constants = new StructureConstants();

    public NetworkBuilder addData(DataHandler dataHandler) {
        this.data.add(dataHandler);
        return this;
    }

    public <T> NetworkBuilder addConstant(ConstantDefinition<T> definition, Function<Random, T> generator) {
        this.constants.addConstant(definition, generator);
        return this;
    }

    public NetworkBuilder globalPredicate(String name, StructurePredicate... predicates) {
        Collections.addAll(this.globalPredicates.computeIfAbsent(name, s -> new ArrayList<>()), predicates);
        return this;
    }

    public StructureNetwork build(List<BuilderNode.Entry<Structure>> rootNodes) {
        return new StructureNetwork(rootNodes, this.data, this.globalPredicates, this.constants);
    }


}
