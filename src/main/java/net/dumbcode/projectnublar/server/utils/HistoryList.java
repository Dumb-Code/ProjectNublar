package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

public class HistoryList<E> {
    private final List<E> list = Lists.newLinkedList();
    private int index = -1;

    public boolean canUndo() {
        return this.index >= 0;
    }

    public boolean canRedo() {
        return this.index < this.list.size()-1;
    }

    public void undo() {
        if(this.canUndo()) {
            this.index--;
        }
    }

    public void redo() {
        if(canRedo()) {
            this.index++;
        }
    }

    public List<E> getUnindexedList() {
        return this.list;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public E get() {
        return this.list.get(this.index);
    }

    public boolean add(E element) {
        while(this.list.size() > this.index + 1) {
            this.list.remove(this.list.size() - 1);
        }
        this.index++;
        return this.list.add(element);
    }

    public void forEach(Consumer<E> consumer) {
        for (int i = 0; i < this.list.size(); i++) {
            if(i <= this.index) {
                consumer.accept(this.list.get(i));
            }
        }
    }

    public void clear() {
        this.list.clear();
        this.index = -1;
    }
}
