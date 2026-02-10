package org.valkyrienskies.core.impl.shadow;

import java.util.Iterator;

public class fj<E> implements Iterable<E> {
    public final Iterable<E> a;

    private fj() {
        this.a = this;
    }

    private fj(final Iterable<E> a) {
        this.a = a;
    }

    public static <T> fj<T> a(final Iterable<T> iterable) {
        if (iterable instanceof fj) {
            return (fj<T>) iterable;
        }
        return new fj<T>(iterable);
    }

    @Override
    public Iterator<E> iterator() {
        return this.a.iterator();
    }

    @Override
    public String toString() {
        return this.a.toString();
    }
}
