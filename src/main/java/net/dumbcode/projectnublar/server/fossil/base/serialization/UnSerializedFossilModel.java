package net.dumbcode.projectnublar.server.fossil.base.serialization;

import net.dumbcode.projectnublar.server.fossil.base.StoneType;

public class UnSerializedFossilModel {
    String sideTexture;
    String bottomTexture;
    String topTexture;
    String particleTexture;
    String fossilTexture;
    int tint;

    public UnSerializedFossilModel(StoneType stoneType, String fossilTexture, int tint) {
        this.sideTexture = stoneType.sideTexture.toString();
        this.bottomTexture = stoneType.bottomTexture.toString();
        this.topTexture = stoneType.topTexture.toString();
        this.particleTexture = stoneType.particleTexture.toString();
        this.fossilTexture = fossilTexture;
        this.tint = tint;
    }
}
