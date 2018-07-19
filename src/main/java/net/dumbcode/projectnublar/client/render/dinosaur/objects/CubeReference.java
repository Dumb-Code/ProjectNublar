package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import lombok.Value;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;

@Value
public class CubeReference {

    float rotationX;
    float rotationY;
    float rotationZ;

    float positionX;
    float positionY;
    float positionZ;

    public static CubeReference fromCube(AdvancedModelRenderer cuboid) {
        return new CubeReference(
                cuboid.defaultRotationX,
                cuboid.defaultRotationY,
                cuboid.defaultRotationZ,
                cuboid.defaultPositionX,
                cuboid.defaultPositionY,
                cuboid.defaultPositionZ
        );
    }
}
