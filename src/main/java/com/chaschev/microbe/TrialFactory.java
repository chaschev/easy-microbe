package com.chaschev.microbe;

/**
* User: chaschev
* Date: 9/5/13
*/
public abstract class TrialFactory {
    protected boolean sortTrialEnabled = true;
    public boolean interimGCEnabled = true;

    public abstract Microbe.Trial create(int trialIndex);

    public boolean isSortTrialEnabled() {
        return sortTrialEnabled;
    }
}
