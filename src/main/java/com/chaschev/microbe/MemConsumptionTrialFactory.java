package com.chaschev.microbe;

import java.lang.reflect.Array;

/**
* User: chaschev
* Date: 5/18/13
*/

public class MemConsumptionTrialFactory<T> implements MicroBenchmark.TrialFactory {
    public static abstract class ObjectFactory<T> {
        public abstract T create(int trialIndex);
        public boolean isGranular(){
            return false;
        }
        public int granularity(int trialIndex){
            throw new UnsupportedOperationException("todo: if T is a list, then this method should return it's size");
        }
    }
    
    private ObjectFactory<T> factory;
    private Class<? extends T> aClass;
    private final int numberOfObjects;
    protected String memMessage;

    public MemConsumptionTrialFactory(ObjectFactory<T> factory, Class<T> aClass, int numberOfObjects) {
        this.factory = factory;
        this.aClass = aClass;
        this.numberOfObjects = numberOfObjects;
        memMessage = "bytes per " + aClass.getSimpleName();
    }

    @Override
    public MicroBenchmark.Trial create(final int trialIndex) {
        return new MicroBenchmark.AbstractTrial() {
            /**
             * Holds objects not to be cleared by GC.
             */
            T[] holders = (T[]) Array.newInstance(aClass, numberOfObjects);

            public Measurements run() {
                for (int i = 0; i < numberOfObjects; i++) {
                    holders[i] = factory.create(trialIndex);
                }

                return new MeasurementsImpl();
            }

            public void addResultsAfterCompletion(Measurements result) {
                result.add(new MeasurementsImpl.Value(result.getMemory() / numberOfObjects, memMessage, Measurements.TYPE_MEMORY));

                if(factory.isGranular()){
                    long itemCount = 0;

                    for(int i = 0;i<numberOfObjects;i++){
                        itemCount += factory.granularity(i);
                    }

                    result.add(new MeasurementsImpl.Value(result.getMemory() / itemCount, "bytes per item", Measurements.TYPE_MEMORY));
                }
            }


        };
    }
}
