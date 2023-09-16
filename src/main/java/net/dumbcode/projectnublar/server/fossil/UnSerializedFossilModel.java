package net.dumbcode.projectnublar.server.fossil;

public class UnSerializedFossilModel {
    String stoneTexture;
    String fossilTexture;
    int tint;

    public UnSerializedFossilModel(String stoneTexture, String fossilTexture, int tint) {
        this.stoneTexture = stoneTexture;
        this.fossilTexture = fossilTexture;
        this.tint = tint;
    }
}
