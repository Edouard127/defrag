package com.defrag.client.mixin.client.baritone;

import baritone.api.Settings;
import com.defrag.client.event.events.BaritoneSettingsInitEvent;
import com.defrag.client.util.BaritoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Settings.class, remap = false)
public class MixinBaritoneSettings {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void baritoneSettingsInit(CallbackInfo ci) {
        BaritoneUtils.INSTANCE.setInitialized(true);
        BaritoneSettingsInitEvent.INSTANCE.post();
    }
}
