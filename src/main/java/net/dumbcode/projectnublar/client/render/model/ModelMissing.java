package net.dumbcode.projectnublar.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMissing extends ModelBase {

    public static ModelMissing INSTANCE = new ModelMissing();

    private ModelRenderer cube;

    private ModelMissing() {
        this.cube = new ModelRenderer(this, 0, 0);
        this.cube.addBox(-8F, 8F, -8F, 16, 16, 16, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.cube.render(scale);
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
