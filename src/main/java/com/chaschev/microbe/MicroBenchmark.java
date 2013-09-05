package com.chaschev.microbe;

import org.apache.commons.lang3.time.StopWatch;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
* User: Admin
* Date: 20.11.11
*/
public class MicroBenchmark {
    int warmUpTrials = 4;
    int numberOfTrials;

    TrialFactory factory;

    public MicroBenchmark(int numberOfTrials, TrialFactory factory) {
        this.numberOfTrials = numberOfTrials;
        this.factory = factory;
    }

    public static interface Trial {
        Trial prepare();

        Measurements run();

        //this one is used for memory usage statistics
        void releaseMemory();

        void addResultsAfterCompletion(Measurements result);
    }
    
    public static abstract class AbstractTrial implements Trial{
        public Trial prepare() {
            return this;
        }

        public void releaseMemory() {
        }
    }

    public static interface TrialFactory {
        Trial create(int trialIndex);
    }


    private static long gcAndGetMemory(StopWatch gcSW) {
        gcSW.resume();
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        final long r = runtime.totalMemory() - runtime.freeMemory();
        gcSW.suspend();

        return r;
    }

    public void runTrials() {
        List<Measurements> results = new ArrayList<Measurements>(numberOfTrials);

        Measurements previousResult = null;

        Clusterables.Statistics statistics = new Clusterables.Statistics();

        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        statistics
            .addOsBefore(osMXBean)
            .addRuntime(runtimeMXBean);

        System.out.println("warming up...");
        for (int i = 0; i < warmUpTrials; i++) {
            System.out.printf("\r%d/%d", i + 1, warmUpTrials);

            final Trial warmupTrial = factory.create(-1).prepare();

            warmupTrial.run();

            warmupTrial.releaseMemory();
        }

        System.out.println("\nrunning trials...");

        StopWatch sw = new StopWatch();
        StopWatch gcSW = new StopWatch();
        sw.start();
        gcSW.start();
        gcSW.suspend();

        for (int i = 0; i < numberOfTrials; i++) {
            System.out.printf("\r%d/%d", i + 1, numberOfTrials);

            Trial trial = factory.create(i).prepare();

            long memBefore = gcAndGetMemory(gcSW);
            long startedAt = System.currentTimeMillis();

            Measurements result = trial.run();

            long endedAt = System.currentTimeMillis();
            long memAfter = gcAndGetMemory(gcSW);

            result.addMemoryAndCpu(memAfter - memBefore, endedAt - startedAt);

            trial.addResultsAfterCompletion(result);

//            if(previousResult != null){
//                result.setLabels(previousResult.getLabels());
//            }

            trial.releaseMemory();

            results.add(result);

            previousResult = result;
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
    }

    public MicroBenchmark setWarmUpTrials(int warmUpTrials) {
        this.warmUpTrials = warmUpTrials;
        return this;
    }

    public MicroBenchmark setNumberOfTrials(int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
        return this;
    }
}
