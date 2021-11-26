
package com.lambda.client.module.modules.events;

import com.lambda.client.module.modules.EventStage;

public class KeyEvent
extends EventStage {
    public boolean info;
    public boolean pressed;

    public KeyEvent(int stage, boolean info, boolean pressed) {
        super(stage);
        this.info = info;
        this.pressed = pressed;
    }
}

