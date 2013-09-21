package com.chaschev.microbe;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Admin
 * Date: 19.11.11
 */
public class MeasurementsImpl implements Measurements {
    public static MeasurementsImpl copyNonValues(Measurements m) {
        MeasurementsImpl r = new MeasurementsImpl();
        MeasurementsImpl source= (MeasurementsImpl) m;

        r.cpuIndex = source.cpuIndex;
        r.memoryIndex = source.memoryIndex;

        return r;
    }

    public static MeasurementsImpl newWithLabelsFrom(Measurements measurements, double... values) {
        measurements = measurements.dup();
        final MeasurementsImpl r = copyNonValues(measurements);

        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            final Value value = measurements.getValue(i);

            r.add(new Value(v, value.label, value.type));
        }

        return r;
    }

    List<Value> values;

    int memoryIndex;
    int cpuIndex;

    public static final MeasurementsImpl EMPTY = new MeasurementsImpl();

    public MeasurementsImpl(Value... values) {
        this.values = Lists.newArrayList(values);
    }

    public MeasurementsImpl(List<Value> values) {
        this.values = values;
    }

    public MeasurementsImpl() {
        this.values = new ArrayList<Value>(8);
    }

    public double get(int i) {
        return values.get(i).v;
    }

    public String getLabel(int i) {
        return values.get(i).label;
    }

    public void addMemoryAndCpu(long memAfterNoGC, long memory, long cpuMs) {
        add(new Value(memory, "mem (with GC, cons.)", TYPE_MEMORY));
        add(new Value(memAfterNoGC, "mem (no GC, alloc.)", TYPE_MEMORY));
        add(new Value(cpuMs, "total cpu, ms", TYPE_TIME));

        memoryIndex = values.size() - 2;
        cpuIndex = values.size() - 1;
    }

    public long getMemory() {
        return (long) values.get(memoryIndex).v;
    }

    public long getCpu() {
        return (long) values.get(cpuIndex).v;
    }

    public void addCoordinates(float[] values, String[] labels) {
        addCoordinates(values, labels, TYPE_NUMBER);
    }

    @Override
    public MeasurementsImpl addCoordinates(float[] values, String[] labels, String type) {
        for (int i = 0; i < values.length; i++) {
            this.values.add(new Value(values[i], labels[i], type));
        }

        return this;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Value getValue(int i) {
        return values.get(i);
    }

    @Override
    public void add(Value value) {
        values.add(value);
    }

    @Override
    public Measurements dup() {
        return new MeasurementsImpl(Lists.newArrayList(Lists.transform(values, new Function<Value, Value>() {
            @Override
            public Value apply( Value input) {
                return input.dup();
            }
        })));
    }

    @Override
    public boolean isMemory(int i) {
        return values.get(i).isMemory();
    }
}
