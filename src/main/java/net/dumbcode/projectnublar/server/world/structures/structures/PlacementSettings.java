package net.dumbcode.projectnublar.server.world.structures.structures;

import com.google.common.collect.Iterables;
import lombok.Value;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.*;

public class PlacementSettings {
    private final Set<Mirror> acceptedMirrors = new HashSet<>();
    private final Set<Rotation> acceptedRotation = new HashSet<>();

    public PlacementSettings addRotation(Rotation... rotation) {
        Collections.addAll(this.acceptedRotation, rotation);
        return this;
    }

    public PlacementSettings addMirror(Mirror... mirror) {
        Collections.addAll(this.acceptedMirrors, mirror);
        return this;
    }

    public PlacementSettings removeRotation(Rotation... rotation) {
        this.acceptedRotation.removeAll(Arrays.asList(rotation));
        return this;
    }

    public PlacementSettings removeMirror(Mirror... mirror) {
        this.acceptedMirrors.removeAll(Arrays.asList(mirror));
        return this;
    }

    public void fillUp() {
        this.addRotation(Rotation.values());
        this.addMirror(Mirror.values());
    }

    public Decision makeDecision(Random random) {
        return new Decision(
            this.acceptedMirrors.isEmpty() ? Mirror.NONE : Iterables.get(this.acceptedMirrors, random.nextInt(this.acceptedMirrors.size())),
            this.acceptedRotation.isEmpty() ? Rotation.NONE : Iterables.get(this.acceptedRotation, random.nextInt(this.acceptedRotation.size()))
        );
    }

    @Value
    public static class Decision {
        private final Mirror mirror;
        private final Rotation rotation;
    }

}
