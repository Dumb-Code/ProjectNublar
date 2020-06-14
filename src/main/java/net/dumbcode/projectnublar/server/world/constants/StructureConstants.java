package net.dumbcode.projectnublar.server.world.constants;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StructureConstants {
    private final List<Entry<?>> entries = new ArrayList<>();

    public Decision createDecision(Random rand) {
        List<DecidedEntry<?>> collect = this.entries.stream().map(e -> e.generate(rand)).collect(Collectors.toList());
        return new Decision() {
            @Override
            public <T> Optional<T> getEntry(ConstantDefinition<T> definition) {
                return collect.stream()
                    .filter(e -> e.definition == definition)
                    .map(e -> definition.cast(e.decided))
                    .findAny();
            }
        };
    }

    public <T> void addConstant(ConstantDefinition<T> definition, Function<Random, T> generator) {
        this.entries.add(new Entry<>(definition, generator));
    }

    @RequiredArgsConstructor
    private static class Entry<T> {
        private final ConstantDefinition<T> definition;
        private final Function<Random, T> entries;

        private DecidedEntry<T> generate(Random random) {
            return new DecidedEntry<>(this.definition, this.entries.apply(random));
        }
    }

    @RequiredArgsConstructor
    private static class DecidedEntry<T> {
        private final ConstantDefinition<T>  definition;
        private final T decided;
    }

    public interface Decision {
        <T> Optional<T> getEntry(ConstantDefinition<T> definition);
        default <T> T requireEntry(ConstantDefinition<T> definition) {
            return this.getEntry(definition).orElseThrow(NullPointerException::new);
        }
    }
}
