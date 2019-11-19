package io.github.springroll.dl.beans

import io.github.springroll.dl.Scriptable
import org.springframework.stereotype.Component

@Component
class ScriptableBean implements Scriptable {

    private int interval = 100
    private boolean fixed = false

    void setInterval(int interval) {
        this.interval = interval
    }

    void setFixed(boolean fixed) {
        this.fixed = fixed
    }

    int getInterval() {
        interval
    }

    boolean isFixed() {
        fixed
    }

}
