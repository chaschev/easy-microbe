package com.chaschev.microbe.trial;

import com.chaschev.microbe.Microbe;

/**
* User: chaschev
* Date: 9/6/13
*/
public abstract class AbstractTrial extends Microbe.Trial {

    public Microbe.Trial prepare() {
        return this;
    }

    public void releaseMemory() {
    }
}
