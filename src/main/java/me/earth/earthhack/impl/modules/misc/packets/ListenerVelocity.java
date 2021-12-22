package me.earth.earthhack.impl.modules.misc.packets;

import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.managers.Managers;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntityVelocity;

final class ListenerVelocity extends
        ModuleListener<Packets, PacketEvent.Receive<SPacketEntityVelocity>>
{
    public ListenerVelocity(Packets module)
    {
        super(module,
                PacketEvent.Receive.class,
                Integer.MIN_VALUE,
                SPacketEntityVelocity.class);
    }

    @Override
    public void invoke(PacketEvent.Receive<SPacketEntityVelocity> event)
    {
        if (event.isCancelled() || !module.fastVelocity.getValue())
        {
            return;
        }

        SPacketEntityVelocity packet = event.getPacket();
        Entity entity = Managers.ENTITIES.getEntity(packet.getEntityID());
        if (entity != null)
        {
            event.setCancelled(module.cancelVelocity.getValue());
            entity.setVelocity(packet.getMotionX() / 8000.0,
                               packet.getMotionY() / 8000.0,
                               packet.getMotionZ() / 8000.0);
        }
    }

}
