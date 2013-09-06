package com.chaschev.microbe.trial;

import com.chaschev.microbe.Microbe;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Random;

/**
* User: chaschev
* Date: 9/6/13
*/
public abstract class RandomMapTrial<K,V> extends AbstractTrial{
    protected int elementCount;

    protected Map<K, V> map;
    protected Random random = new Random();

    public RandomMapTrial(int elementCount) {
        this.elementCount = elementCount;
        map = Maps.newHashMapWithExpectedSize(elementCount);
    }

    protected abstract Map.Entry<K, V> createNew(int index);

    @Override
    public Microbe.Trial prepare() {
        map.clear();

        for (int i = 0; i < elementCount; i++) {
            final Map.Entry<K, V> e = createNew(i);
            map.put(e.getKey(), e.getValue());
        }

        return this;
    }
}
