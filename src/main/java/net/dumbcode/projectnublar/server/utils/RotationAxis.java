package net.dumbcode.projectnublar.server.utils;

import javax.vecmath.Vector3f;

public enum RotationAxis {
    X_AXIS(new Vector3f(1f, 0f, 0f)),
    Y_AXIS(new Vector3f(0f, 1f, 0f)),
    Z_AXIS(new Vector3f(0f, 0f, 1f)),
    NONE(new Vector3f(0f, 0f, 0f));

    private Vector3f axis;

    RotationAxis(Vector3f axis) {
        this.axis = axis;
    }

    public Vector3f getAxis() {
        return axis;
    }
}
