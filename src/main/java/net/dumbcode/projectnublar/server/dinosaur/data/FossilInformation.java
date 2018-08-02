package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;

import java.util.List;

@Value
public class FossilInformation {
    private final Dinosaur dinosaur;
    private final String type;
}
