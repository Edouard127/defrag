package me.earth.earthhack.impl.managers.minecraft;

import me.earth.earthhack.api.event.bus.EventListener;
import me.earth.earthhack.api.event.bus.SubscriberImpl;
import me.earth.earthhack.impl.event.events.network.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;

import java.util.ArrayList;

public class TPSManager extends SubscriberImpl {

    private final int maxTicksRecorded = 100;

    private final ArrayList<Float> queue = new ArrayList<>();
    private long time;

    public TPSManager() {
        this.listeners.add(
                new EventListener<PacketEvent.Receive<SPacketTimeUpdate>>
                        (PacketEvent.Receive.class, SPacketTimeUpdate.class) {
                    @Override
                    public void invoke(PacketEvent.Receive<SPacketTimeUpdate> event) {
                        if (time != 0) {
                            synchronized (queue) {
                                if (queue.size() > maxTicksRecorded) {
                                    queue.remove(0);
                                }
                                queue.add((float) (System.currentTimeMillis() - time));
                            }
                        }

                        time = System.currentTimeMillis();
                    }
                });
    }

    public synchronized float getTps(int seconds) {

        synchronized (queue) {
            if (queue.size() < 2) {
                return 20f;
            }
        }

        // todo: does this actually calculate the tps? it shows numbers that are believable but still not sure lmfao


        float factor = 0.0f;
        int detects;

        synchronized (queue) {
            detects = Math.min(seconds, queue.size() - 1);
            for (int i = 0; i < detects; i++) {
                factor += Math.max(0.0f, queue.get(i));
            }
        }

        factor /= detects * 50;

        return 40 - factor;
    }

    public synchronized float getTps() {
        return getTps(20);
    }

}
