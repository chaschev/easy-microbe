package com.chaschev.microbe;

import com.chaschev.microbe.trial.AbstractTrial;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.*;

/**
* User: Admin
* Date: 20.11.11
*/
public class Microbe {
    String name;
    int warmUpTrials = 4;
    int numberOfTrials;

    public static <T> Microbe newMicroMem(String name, Class<T> aClass, int numberOfObjects, ObjectFactory<T> factory){
        return new Microbe(-1, name,
            new MemConsumptionTrialFactory<T>(
                factory,
                aClass, numberOfObjects)
        );
    }

    public static Microbe newMicroCpu(String name, int numberOfTrials, TrialFactory factory){
        factory.interimGCEnabled = false;

        return new Microbe(numberOfTrials, name, factory);
    }

    public static class SortOptions{
        int trials = 200;
        int warmUpTrials = 100;
        int elementCount = 10000;
    }

    SortOptions sortOptions = new SortOptions();

    TrialFactory factory;

    TrialFactory sortFactory = new TrialFactory() {
        {
            sortTrialEnabled = false;
            interimGCEnabled = false;
        }

        @Override
        public Trial create(int trialIndex) {
            return new AbstractTrial() {

                ArrayList<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(sortOptions.elementCount);
                ArrayList<Trial> subTrials = new ArrayList<Trial>(sortOptions.elementCount);

                @Override
                public Trial prepare() {
                    final Random r = new Random();

                    for (int i = 0; i < sortOptions.elementCount; i++) {
                         list.add(new AbstractMap.SimpleEntry<Integer, Integer>(i, r.nextInt()));
                         subTrials.add(factory.create(i));
                    }

                    return this;
                }

                @Override
                public Measurements run(int trialIndex) {
                    Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
                        @Override
                        public int compare(Map.Entry<Integer, Integer> e1, Map.Entry<Integer, Integer> e2) {
                            final Integer i1 = e1.getKey();
                            final Integer i2 = e2.getKey();
                            subTrials.get(i1).prepare().run(i1);
                            subTrials.get(i2).prepare().run(i2);

                            return Integer.compare(e1.getValue(), e2.getValue());
                        }
                    });

                    return new MeasurementsImpl();
                }

                @Override
                public void addResultsAfterCompletion(Measurements result) {

                }
            };
        }
    };

    public Microbe(int numberOfTrials, String name, TrialFactory factory) {
        this.numberOfTrials = numberOfTrials;
        this.name = name;
        this.factory = factory;
    }

    public abstract static class Trial {
        public abstract Trial prepare();

        public abstract Measurements run(int trialIndex);

        //this one is used for memory usage statistics
        public abstract void releaseMemory();

        public void addResultsAfterCompletion(Measurements result){}
    }

    private static long gcAndGetMemory(StopWatch gcSW) {
        gcSW.resume();
        System.gc();
        final long r = getMemory();
        gcSW.suspend();

        return r;
    }

    public static long getMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public void runTrials() {
        List<Measurements> results = new ArrayList<Measurements>(numberOfTrials);

        Clusterables.Statistics statistics = new Clusterables.Statistics();

        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        statistics
            .addOsBefore(osMXBean)
            .addRuntime(runtimeMXBean);

        System.out.println("running trial: " + name);
        System.out.println("warming up...");

        final int printEachWarmX = calcPrintingPeriod(warmUpTrials);

        for (int i = 0; i < warmUpTrials; i++) {
            if (i % printEachWarmX == 0) {
                System.out.printf("\r%d/%d", i + 1, warmUpTrials);
            }

            final Trial warmupTrial = factory.create(-1).prepare();

            warmupTrial.run(i);

            warmupTrial.releaseMemory();
        }

        System.out.println("\nrunning trials...");

        StopWatch sw = new StopWatch();
        StopWatch gcSW = new StopWatch();
        sw.start();
        gcSW.start();
        gcSW.suspend();

        long memBefore = gcAndGetMemory(gcSW);

        final int printEachX = calcPrintingPeriod(numberOfTrials);

        for (int i = 0; i < numberOfTrials; i++) {
            if(i % printEachX == 0){
                System.out.printf("\r%d/%d", i + 1, numberOfTrials);
            }

            Trial trial = factory.create(i).prepare();

            if(factory.interimGCEnabled){
                memBefore = gcAndGetMemory(gcSW);
            }

            long startedAt = System.currentTimeMillis();

            Measurements result = trial.run(i);

            long memAfterNoGC = getMemory();

            long endedAt = System.currentTimeMillis();

            long memAfter;
            if(factory.interimGCEnabled){
                memAfter = gcAndGetMemory(gcSW);
            }else{
                memAfter = memAfterNoGC;
            }

            result = result.dup();

            result.addMemoryAndCpu(memAfterNoGC - memBefore, memAfter - memBefore, endedAt - startedAt);

            trial.addResultsAfterCompletion(result);

            trial.releaseMemory();

            results.add(result);
        }

        sw.stop();

        System.out.println();

        statistics.addOsAfter(osMXBean);

        Clusterables.Statistics.addMeasurements(statistics, results);

        System.out.println(statistics);

        final long totalTime = sw.getTime();
        final long gcTime = gcSW.getTime();

        System.out.printf(
            "non-gc time: %dms (%.1f%%), gc: %s (%.1f%%) done in %s%n",
            totalTime - gcTime, (totalTime - gcTime) * 100D / totalTime,
            gcSW, gcTime * 100D / totalTime, sw);

        System.out.println();

        if(factory.sortTrialEnabled && sortOptions != null){
            new Microbe(sortOptions.trials, name + " - sort test (adding operation in test into sorting of " + sortOptions.elementCount +
                "-elements array)", sortFactory)
                .setWarmUpTrials(sortOptions.warmUpTrials)
                .noSort()
                .runTrials();
        }
    }

    private static int calcPrintingPeriod(int trialCount) {
        return trialCount > 500 ? Math.max(trialCount / 200, 1) : 1;
    }

    public Microbe noSort() {
        sortOptions = null;
        return this;
    }

    public Microbe setWarmUpTrials(int warmUpTrials) {
        this.warmUpTrials = warmUpTrials;
        return this;
    }

    public Microbe setNumberOfTrials(int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
        return this;
    }
}
