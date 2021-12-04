package com.defrag.client.mixin.client.network;

import com.defrag.client.event.DefragEventBus;
import com.defrag.client.event.events.PacketEvent;
import com.defrag.client.module.modules.player.NoPacketKick;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketPre(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        DefragEventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void sendPacketPost(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.PostSend(packet);
        DefragEventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Receive(packet);
        DefragEventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void channelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.PostReceive(packet);
        DefragEventBus.INSTANCE.post(event);
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        if (NoPacketKick.INSTANCE.isEnabled()) {
            NoPacketKick.sendWarning(throwable);
            ci.cancel();
        }
    }

}
