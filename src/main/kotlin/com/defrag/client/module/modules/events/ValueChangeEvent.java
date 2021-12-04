
package com.defrag.client.module.modules.events;

import com.defrag.client.module.modules.EventStage;
import me.earth.phobos.features.setting.Setting;

public class ValueChangeEvent
extends EventStage {
    public Setting setting;
    public Object value;

    public ValueChangeEvent(Setting setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}

