package com.chaschev.microbe.trial;

import com.chaschev.microbe.Microbe;

import java.util.ArrayList;
import java.util.Random;

/**
* User: chaschev
* Date: 9/6/13
*/
public abstract class RandomArrayListTrial<T> extends AbstractTrial{
    protected int elementCount;

    protected ArrayList<T> arrayList;
    protected Random random = new Random();

    public RandomArrayListTrial(int elementCount) {
        this.elementCount = elementCount;
        arrayList = new ArrayList<T>(elementCount);
    }

    protected abstract T createNew(int index);

    @Override
    public Microbe.Trial prepare() {
        arrayList.clear();

        for (int i = 0; i < elementCount; i++) {
             arrayList.add(createNew(i));
        }

        return this;
    }
}
