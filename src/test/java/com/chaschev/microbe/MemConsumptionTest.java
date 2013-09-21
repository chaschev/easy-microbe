package com.chaschev.microbe;

//import gnu.trove.list.array.TIntArrayList;
//import gnu.trove.map.hash.TIntIntHashMap;

import com.chaschev.microbe.trial.AbstractTrial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Admin
 * Date: 19.11.11
 */

public class MemConsumptionTest {
    public static final Logger logger = LoggerFactory.getLogger(MemConsumptionTest.class);

    public static void main(String[] args) {
        MemConsumptionTest mem = new MemConsumptionTest();

//        mem.arrayListVsLinkedList();
        new TryCatchMicrobe(1, 100, true).run();
        new TryCatchMicrobe(90, 100, true).run();
        new TryCatchMicrobe(90, 100, false).run();
//        mem.testStringMemoryConsumption();
//        mem.testObjectMemoryConsumption();
//        mem.testByteMemoryConsumption();
//        mem.test20ByteMemoryConsumption();
//        mem.test20byteMemoryConsumption();
//        mem.testMapsMemoryConsumption();
//        mem.testByteArrayMemoryConsumption();
//        mem.testDoubleMemoryConsumption();
//        mem.testStringsMemoryConsumption();
    }

    /**
     * This test shows that having try-catch and no-try-catch are approximately the same in speed.
     * It also shows that integer division is much slower than catching an exception.
     */
    public static class TryCatchMicrobe{
        @Param({"1", "10"}) int m = 1;

        @Param("100") int n = 100;

        @Param({"true", "false"}) boolean useTryCatch;

        public TryCatchMicrobe(int m, int n, boolean useTryCatch) {
            this.m = m;
            this.n = n;
            this.useTryCatch = useTryCatch;
        }

        public void run(){
            final class Foo{
                int i;
            }

            final Foo obj = new Foo();

            final AbstractTrial trial = new AbstractTrial() {
                int errorCount = 0;

                @Override
                public Measurements run(int i) {

                    if (useTryCatch) {
                        try {
                            obj.i = i / (i % n < m ? 0 : 1);
                        } catch (Exception e) {
                            errorCount++;
                        }
                    } else {
                        final int divisor = i % n < m ? 0 : 1;
                        if (divisor != 0) {
                            obj.i = i / divisor;
                        } else {
                            errorCount++;
                            obj.i++;
                        }
                    }

                    return new MeasurementsImpl();
                }

                @Override
                public void addResultsAfterCompletion(Measurements result) {
                    result.add(new Value(errorCount, "error count"));
                }
            };

            System.out.println(this);

            Microbe.newMicroCpu("tryCatch", 200000, new TrialFactory() {
                @Override
                public Microbe.Trial create(int trialIndex) {
                    return trial;
                }
            })  .setWarmUpTrials(200000)
                .runTrials();
        }

        @Override
        public String toString() {
            return String.format("=== Error pct: %.1f%%, try-catch used: %s%n", 100d * m / n, useTryCatch);
        }
    }



    public void arrayListVsLinkedList() {
        final int listSize = 50000;

        final int numberOfTrials = 3;
        final int warmUpTrials = 1;

        //1085K, 24.5s
        Microbe.newMicroMem("ArrayList", List.class, 50, new ObjectFactory<List>(listSize) {
            @Override
            public List create(int trialIndex) {
                List<Integer> integers = new ArrayList<Integer>();

                for (int i = trialIndex; i < trialIndex + listSize; i++) {
                    integers.add(i * i + trialIndex);
                }

                return integers;
            }
        }).setNumberOfTrials(numberOfTrials)
            .setWarmUpTrials(warmUpTrials)
            .runTrials();

        //2003K, 25.4s
        Microbe.newMicroMem("LinkedList", List.class, 50, new ObjectFactory<List>(listSize) {
            @Override
            public List create(int trialIndex) {
                List<Integer> integers = new LinkedList<Integer>();

                for (int i = trialIndex; i < trialIndex + listSize; i++) {
                    integers.add(i * i + trialIndex);
                }

                return integers;
            }
        })  .setNumberOfTrials(numberOfTrials)
            .setWarmUpTrials(warmUpTrials)
            .runTrials();

        System.gc();
//        System.out.println("TIntArrayList");
//        //327K, 15.3s
//        new MicroBenchmark(50,
//            new MemConsumptionTrialFactory<TIntArrayList>(
//                new MemConsumptionTrialFactory.ObjectFactory<TIntArrayList>() {
//                    @Override
//                    public TIntArrayList create(int trialIndex) {
//                        TIntArrayList integers = new TIntArrayList();
//
//                        for (int i = trialIndex; i < trialIndex + x; i++) {
//                            integers.add(i * i + trialIndex);
//                        }
//
//                        return integers;
//                    }
//                },
//                TIntArrayList.class, 50)
//        ).runTrials();
    }
}
