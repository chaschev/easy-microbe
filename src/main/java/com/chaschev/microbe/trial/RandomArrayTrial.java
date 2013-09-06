package com.chaschev.microbe.trial;

import com.chaschev.microbe.Microbe;

import java.util.Arrays;
import java.util.Random;

/**
* User: chaschev
* Date: 9/6/13
*/
public abstract class RandomArrayTrial<T> extends AbstractTrial{
    protected int elementCount;

    protected T[] array;

    protected Random random = new Random();

    public RandomArrayTrial(int elementCount, T[] array) {
        this.elementCount = elementCount;
        this.array = array;
    }

    protected abstract T createNew(int index);

    @Override
    public Microbe.Trial prepare() {
        Arrays.fill(array, null);

        for (int i = 0; i < elementCount; i++) {
            array[i] = createNew(i);
        }

        return this;
    }
}
