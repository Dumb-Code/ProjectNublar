package net.dumbcode.projectnublar.client.model.fossil;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.client.model.obj.MaterialLibrary;

import java.util.Map;
import java.util.NoSuchElementException;

public class MatLib {
    public Map<String, MaterialLibrary.Material> materials = Maps.newHashMap();


    public MaterialLibrary.Material getMaterial(String mat)
    {
        if (!materials.containsKey(mat))
            throw new NoSuchElementException("The material was not found in the library: " + mat);
        return materials.get(mat);
    }

    public void setMaterials(Map<String, MaterialLibrary.Material> materials) {
        this.materials = materials;
    }
}
