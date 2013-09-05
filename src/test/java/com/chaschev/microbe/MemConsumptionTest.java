package com.chaschev.microbe;

//import gnu.trove.list.array.TIntArrayList;
//import gnu.trove.map.hash.TIntIntHashMap;
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

        mem.arrayListVsLinkedList();
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


    public void arrayListVsLinkedList() {
        System.out.println("linkedList");

        final int listSize = 50000;

        //2003K, 25.4s
        final int numberOfTrials = 3;
        final int warmUpTrials = 1;

        System.out.println("arrayList");
        //1085K, 24.5s
        new MicroBenchmark(numberOfTrials,
            new MemConsumptionTrialFactory<List>(
                new MemConsumptionTrialFactory.ObjectFactory<List>() {
                    @Override
                    public List create(int trialIndex) {
                        List<Integer> integers = new ArrayList<Integer>();

                        for (int i = trialIndex; i < trialIndex + listSize; i++) {
                            integers.add(i * i + trialIndex);
                        }

                        return integers;
                    }

                    @Override
                    public boolean isGranular() {
                        return true;
                    }

                    @Override
                    public int granularity(int trialIndex) {
                        return listSize;
                    }
                },
                List.class, 50)
        )
            .setWarmUpTrials(warmUpTrials)
            .runTrials();

        new MicroBenchmark(numberOfTrials,
            new MemConsumptionTrialFactory<List>(
                new MemConsumptionTrialFactory.ObjectFactory<List>() {
                    @Override
                    public List create(int trialIndex) {
                        List<Integer> integers = new LinkedList<Integer>();

                        for (int i = trialIndex; i < trialIndex + listSize; i++) {
                            integers.add(i * i + trialIndex);
                        }

                        return integers;
                    }

                    @Override
                    public boolean isGranular() {
                        return true;
                    }

                    @Override
                    public int granularity(int trialIndex) {
                        return listSize;
                    }
                },
                List.class, 50)
        )
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
