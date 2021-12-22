package me.earth.earthhack.impl.core.mixins.network.server;

import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketChat.class)
public interface ISPacketChat {

    @Accessor(value = "chatComponent")
    void setComponent(ITextComponent component);
}
