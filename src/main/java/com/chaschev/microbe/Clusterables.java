package com.chaschev.microbe;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Formatter;
import java.util.List;

import static com.chaschev.chutils.io.FileUtils.humanReadableByteCount;
import static com.chaschev.microbe.MeasurementsImpl.newWithLabelsFrom;

/**
* User: Admin
* Date: 20.11.11
*/
public class Clusterables {
    public static Measurements min(Measurements a, Measurements b) {
        Measurements r = MeasurementsImpl.copyNonValues(a);

        for (int i = 0; i < a.size(); i++) {
            Value v = a.getValue(i);

            r.add(new Value(Math.min(a.get(i), b.get(i)), v.label, v.type));
        }

        return r;
    }

    public static Measurements max(Measurements a, Measurements b) {
        Measurements r = MeasurementsImpl.copyNonValues(a);

        for (int i = 0; i < a.size(); i++) {
            Value v = a.getValue(i);

            r.add(new Value(Math.max(a.get(i), b.get(i)), v.label, v.type));
        }

        return r;
    }

    public static class Statistics {
        Measurements avg, min, max, stddev;
        protected String os;
        private String arch;
        private int processors;
        private double loadBeforeRun;
        private double loadAfterRun;
        private String vm;

        public Statistics init(Measurements avg, Measurements min, Measurements max) {
            this.avg = avg;
            this.min = min;
            this.max = max;

            return this;
        }

        public void setStddev(Measurements stddev) {
            this.stddev = stddev;
        }

        public static Statistics addMeasurements(Statistics statistics, List<Measurements> list) {
            avgMinMax(statistics, list);
            statistics.setStddev(stddev(list));
            return statistics;
        }

        @Override
        public String toString() {
            Formatter formatter = new Formatter();

            int coordinateCount = avg.size();

            formatter.format("%20s | %12s | %12s | %12s | %12s%n",
                    "x","min", "avg", "max", "stddev");

            for (int i = 0; i < coordinateCount; i++) {
                if(min.isMemory(i)){
                    formatter.format("%20s | %12s | %12s | %12s | %12s%n", avg.getLabel(i),
                        humanReadableByteCount(min.get(i), false).toString(2),
                        humanReadableByteCount(avg.get(i), false).toString(2),
                        humanReadableByteCount(max.get(i), false).toString(2),
                        humanReadableByteCount(stddev.get(i), false).toString(2)
                    );
                }else{
                    formatter.format("%20s | %12.5f | %12.5f | %12.5f | %12.5f%n", avg.getLabel(i),
                        min.get(i),
                        avg.get(i),
                        max.get(i),
                        stddev.get(i)
                    );
                }

            }

            return formatter.toString();
        }


        public Statistics addOsBefore(OperatingSystemMXBean osMXBean) {
            this.loadBeforeRun = osMXBean.getSystemLoadAverage();
            this.os = osMXBean.getName() + " " + osMXBean.getVersion();
            this.arch = osMXBean.getArch();
            this.processors = osMXBean.getAvailableProcessors();

            return this;
        }

        public Statistics addOsAfter(OperatingSystemMXBean osMXBean) {
            this.loadAfterRun = osMXBean.getSystemLoadAverage();

            return this;
        }

        public Statistics addRuntime(RuntimeMXBean runtimeMXBean) {
            this.vm = runtimeMXBean.getVmVendor() +
                " " + runtimeMXBean.getVmName() + " " + runtimeMXBean.getVmVersion();

            return this;
        }
    }

    public static Statistics avgMinMax(Statistics stats, List<Measurements> measurementsList) {
        int coordinateCount = measurementsList.get(0).size();
        double[] sum = new double[coordinateCount];
        double[] min = new double[coordinateCount];
        double[] max = new double[coordinateCount];

        for (int i = 0; i < min.length; i++) {
            min[i] = Float.MAX_VALUE;
        }

        for (Measurements measurements : measurementsList) {
            for (int i = 0; i < coordinateCount; i++) {
                updateSumMinMax(sum, min, max, measurements.get(i), i);
            }
        }

        double[] floatMeans = new double[coordinateCount];

        for (int i = 0; i < coordinateCount; i++) {
            sum[i] /= measurementsList.size();
        }

        copyToFloat(coordinateCount, sum, floatMeans);

        newWithLabelsFrom(measurementsList.get(0), floatMeans);

        return stats.init(
            newWithLabelsFrom(measurementsList.get(0), floatMeans),
            newWithLabelsFrom(measurementsList.get(0), ArrayUtils.subarray(min, 0, coordinateCount)),
            newWithLabelsFrom(measurementsList.get(0), ArrayUtils.subarray(max, 0, coordinateCount))
        );
    }

    private static void copyToFloat(int coordinateCount, double[] sum, double[] floatMeans) {
        for (int i = 0; i < coordinateCount; i++) {
            floatMeans[i] = sum[i];
        }
    }

    private static void updateSumMinMax(double[] sum, double[] min, double[] max, double v, int i) {
        sum[i] += v;
        min[i] = Math.min(min[i], v);
        max[i] = Math.max(max[i], v);
    }

    public static Measurements variance(List<Measurements> list) {
        int coordinateCount = list.get(0).size();

        int n = 0;
        double[] mean = new double[coordinateCount ];
        double[] sums = new double[coordinateCount ];

        for (int i = 0; i < list.size(); i++) {
            Measurements measurements = list.get(i);

            n++;

            for (int j = 0; j < coordinateCount; j++) {
                updateVariance(mean, sums, n, measurements.get(j), j);
            }
        }

        double[] floatMeans = new double[sums.length];

        for (int i = 0; i < sums.length; i++) {
            floatMeans[i] = (sums[i] / list.size());
        }

        return newWithLabelsFrom(list.get(0), floatMeans);
    }

    public static Measurements stddev(List<Measurements> list) {
        Measurements variance = variance(list);

        final int size = variance.size();

        double[] values = new double[size];

        for (int i = 0; i < size; i++) {
            values[i] = Math.sqrt(variance.get(i));
        }

        return newWithLabelsFrom(list.get(0), values);
    }


    private static void updateVariance(double[] mean, double[] sums, int n, double x, int j) {
        double delta = x - mean[j];
        mean[j] += delta / n;
        sums[j] += delta * (x - mean[j]);
    }

}
