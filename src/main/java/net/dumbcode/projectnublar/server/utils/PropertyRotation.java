package net.dumbcode.projectnublar.server.utils;

import com.google.common.base.Optional;
import net.minecraft.block.properties.PropertyHelper;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//Todo: have a degree of accuracy, ect 2, 1, 0.5, 0.2. Would mean converting to a double
public class PropertyRotation extends PropertyHelper<Integer> {

    protected PropertyRotation(String name) {
        super(name, Integer.class);
    }

    @Override
    public Collection<Integer> getAllowedValues() {
        return IntStream.range(0, 360).boxed().collect(Collectors.toList());
    }

    @Override
    public Optional<Integer> parseValue(String value) {
        try {
            return Optional.of(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
    }

    @Override
    public String getName(Integer value) {
        return value.toString();
    }

    public static PropertyRotation create(String name) {
        return new PropertyRotation(name);
    }
}
