package me.earth.earthhack.impl.modules.misc.logger;

import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.util.TextUtil;
import me.earth.earthhack.impl.Earthhack;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.modules.misc.logger.util.LoggerMode;
import me.earth.earthhack.impl.util.helpers.addable.RegisteringModule;
import me.earth.earthhack.impl.util.helpers.addable.setting.SimpleRemovingSetting;
import me.earth.earthhack.impl.util.mcp.MappingProvider;
import me.earth.earthhack.impl.util.network.PacketUtil;
import me.earth.earthhack.impl.util.text.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class Logger extends RegisteringModule<Boolean, SimpleRemovingSetting>
{
    protected final Setting<LoggerMode> mode =
            register(new EnumSetting<>("Mode", LoggerMode.Normal));
    protected final Setting<Boolean> incoming    =
            register(new BooleanSetting("Incoming", true));
    protected final Setting<Boolean> outgoing    =
            register(new BooleanSetting("Outgoing", true));
    protected final Setting<Boolean> info        =
            register(new BooleanSetting("Info", true));
    protected final Setting<Boolean> chat        =
            register(new BooleanSetting("Chat", false));
    protected final Setting<Boolean> deobfuscate =
            register(new BooleanSetting("Deobfuscate", true));
    protected final Setting<Boolean> stackTrace =
            register(new BooleanSetting("StackTrace", false));
    protected final Setting<Boolean> statics =
            register(new BooleanSetting("Static", false));

    protected final Setting<Boolean> filter =
            registerBefore(new BooleanSetting("Filter", false), listType);

    protected final List<String> packetNames;
    protected boolean cancel;

    public Logger()
    {
        super("Logger",
                Category.Misc,
                "Add_Packet",
                "packet",
                SimpleRemovingSetting::new,
                s -> "Filter " + s.getName() + " packets.");

        packetNames = PacketUtil.getAllPackets()
                                .stream()
                                .map(MappingProvider::simpleName)
                                .collect(Collectors.toList());
        this.listeners.add(new ListenerChatLog(this));
        this.listeners.add(new ListenerReceive(this));
        this.listeners.add(new ListenerSend(this));
        this.setData(new LoggerData(this));
    }

    @Override
    protected void onEnable()
    {
        cancel = false;
    }

    @Override
    public String getInput(String input, boolean add)
    {
        if (add)
        {
            String packet = getPacketStartingWith(input);
            if (packet != null)
            {
                return TextUtil.substring(packet, input.length());
            }

            return "";
        }

        return super.getInput(input, false);
    }

    private String getPacketStartingWith(String input)
    {
        for (String packet : packetNames)
        {
            if (TextUtil.startsWith(packet, input))
            {
                return packet;
            }
        }

        return null;
    }

    public void logPacket(Packet<?> packet, String message, boolean cancelled)
    {
        String simpleName = MappingProvider.simpleName(packet.getClass());
        if (filter.getValue() && !isValid(simpleName))
        {
            return;
        }

        StringBuilder outPut = new StringBuilder(message)
                                        .append(simpleName)
                                        .append(", cancelled : ")
                                        .append(cancelled)
                                        .append("\n");
        if (info.getValue())
        {
            try
            {
                Class<?> clazz = packet.getClass();
                while (clazz != Object.class)
                {
                    for (Field field : clazz.getDeclaredFields())
                    {
                        if (field != null)
                        {
                            if (Modifier.isStatic(field.getModifiers())
                                    && !statics.getValue())
                            {
                                continue;
                            }

                            field.setAccessible(true);
                            outPut.append("     ")
                                  .append(getName(clazz, field))
                                  .append(" : ")
                                  .append(field.get(packet))
                                  .append("\n");
                        }
                    }

                    clazz = clazz.getSuperclass();
                }
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        String s = outPut.toString();
        if (chat.getValue())
        {
            mc.addScheduledTask(() ->
            {
                cancel = true;
                try
                {
                    ChatUtil.sendMessage(s);
                }
                finally
                {
                    cancel = false;
                }
            });
        }

        Earthhack.getLogger().info(s);

        if (stackTrace.getValue())
        {
            Thread.dumpStack();
        }
    }

    private String getName(Class<?> c, Field field)
    {
        if (deobfuscate.getValue())
        {
            String name = MappingProvider.field(c, field.getName());
            if (name != null)
            {
                return name;
            }
        }

        return field.getName();
    }

    public LoggerMode getMode()
    {
        return mode.getValue();
    }

}
