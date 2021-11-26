
package com.lambda.client.module.modules;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class PacketEvent
extends EventStage {
    private int stage;

    public void EventStage() {
    }

    public void EventStage(int stage) {
        this.stage = stage;
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
    private final Packet<?> packet;

    public PacketEvent(int stage, Packet<?> packet) {
        super(stage);
        this.packet = packet;
    }

    public <T extends Packet<?>> T getPacket() {
        return (T)this.packet;
    }

    @Cancelable
    public static class Send
    extends PacketEvent {
        public Send(int stage, Packet<?> packet) {
            super(stage, packet);
        }
    }

    @Cancelable
    public static class Receive
    extends PacketEvent {
        public Receive(int stage, Packet<?> packet) {
            super(stage, packet);
        }
    }
}

