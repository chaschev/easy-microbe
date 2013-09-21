package com.chaschev.microbe;

/**
* User: Admin
* Date: 20.11.11
*/
public interface Measurements {
    String TYPE_NUMBER = "number";
    String TYPE_MEMORY = "mem";
    String TYPE_TIME = "time";

    double get(int i);

    //label for the coordinate
    String getLabel(int i);

    void addMemoryAndCpu(long memAfterNoGC, long memory, long cpuMs);

    long getMemory();
    long getCpu();

    void addCoordinates(float[] values, String[] labels);

    boolean isMemory(int i);

    MeasurementsImpl addCoordinates(float[] values, String[] labels, String type);

    int size();

    Value getValue(int i);

    void add(Value value);

    Measurements dup();
}
