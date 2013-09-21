package com.chaschev.microbe.trial;

import com.chaschev.microbe.Microbe;

import java.util.Random;

/**
* User: chaschev
* Date: 9/6/13
*/
public abstract class RandomIntArrayTrial extends AbstractTrial{
    protected int elementCount;

    protected int[] array;

    protected Random random = new Random();

    public RandomIntArrayTrial(int elementCount) {
        this.elementCount = elementCount;
        this.array = new int[elementCount];
    }

    @Override
    public Microbe.Trial prepare() {
        for (int i = 0; i < elementCount; i++) {
            array[i] = random.nextInt();
        }

        return this;
    }
}
