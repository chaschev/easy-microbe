package com.chaschev.microbe;

/**
* User: chaschev
* Date: 9/5/13
*/
public class Value {
    double v;
    String label;
    String type;

    public Value(double v, String label, String type) {
        this.v = v;
        this.label = label;
        this.type = type;
    }

    public Value(double v, String label) {
        this.v = v;
        this.label = label;
        this.type = Measurements.TYPE_NUMBER;
    }

    public boolean isMemory() {
        return Measurements.TYPE_MEMORY.equals(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Value{");
        sb.append("v=").append(v);
        sb.append(", label='").append(label).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Value dup(){
        return new Value(v, label, type);
    }
}
