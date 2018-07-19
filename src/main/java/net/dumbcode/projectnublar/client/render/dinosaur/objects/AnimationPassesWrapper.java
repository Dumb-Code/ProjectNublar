package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import com.google.common.collect.Lists;
import lombok.val;
import net.dumbcode.projectnublar.server.entity.EntityAnimatable;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Predicate;

public class AnimationPassesWrapper<T extends EntityAnimatable> {
    private List<Pair<Predicate<T>, AnimationPass<T>>> entityList = Lists.newArrayList();

    @SafeVarargs
    public AnimationPassesWrapper(T entity, TabulaModel model, Pair<Predicate<T>, AnimationPass<T>>... pairs) {
        this.entityList = Lists.newArrayList(pairs);
        for (val pair : this.entityList) {
            pair.getRight().init(model, entity);
        }
    }

    public void performAnimations(T entity, float limbSwing, float limbSwingAmount, float ticks) {
        for (val pair : this.entityList) {
            if(pair.getLeft().test(entity)) {
                pair.getRight().performAnimations(entity, limbSwing, limbSwingAmount, ticks);
            }
        }
    }

}
