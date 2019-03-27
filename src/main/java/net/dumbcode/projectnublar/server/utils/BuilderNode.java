package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.Value;

import java.util.List;

public class BuilderNode<T> {

    public static <T> BuilderNode<T> builder(Class<T> reference) {
        return new BuilderNode<>(null, null);
    }

    private final BuilderNode<T> parent;
    private final T element;

    private final List<BuilderNode<T>> children = Lists.newArrayList();

    private BuilderNode(BuilderNode<T> parent, T element) {
        this.parent = parent;
        this.element = element;
    }

    public BuilderNode<T> child(T element) {
        BuilderNode<T> node = new BuilderNode<>(this, element);
        this.children.add(node);
        return node;
    }

    public BuilderNode<T> sibling(T element) {
        BuilderNode<T> node = new BuilderNode<>(this.parent, element);
        this.parent.children.add(node);
        return node;
    }

    public BuilderNode<T> end() {
        return this.parent;
    }


    private Entry<T> generateNode() {
        Entry<T> node = new Entry<>(this.element);
        for (BuilderNode<T> child : this.children) {
            node.children.add(child.generateNode());
        }
        return node;
    }


    public List<Entry<T>> buildToRoots() {
        if(this.parent != null) {
            return this.parent.buildToRoots();
        }
        List<Entry<T>> list = Lists.newArrayList();
        for (BuilderNode<T> child : this.children) {
            list.add(child.generateNode());
        }
        return list;
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder("{ \"name\":\"" + (this.element == null ? "$$null$$" : this.element.toString()) + "\"");

        if(!this.children.isEmpty()) {
            s.append(",\"children\":[");
            boolean first = true;
            for (BuilderNode<T> child : this.children) {
                if(!first) {
                    s.append(",");
                }
                s.append(child);
                first = false;
            }
            s.append("]");
        }

        return s + "}";
    }

    @Value
    public static class Entry<T> { T element; List<Entry<T>> children = Lists.newArrayList(); }

}
