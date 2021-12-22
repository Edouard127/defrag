package me.earth.earthhack.impl.modules.combat.autocrystal.util;

import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class BreakData<T extends CrystalData>
{
    private final Collection<T> data;

    private float fallBackDmg = Float.MAX_VALUE;
    private Entity antiTotem;
    private Entity fallBack;

    public BreakData(Collection<T> data)
    {
        this.data = data;
    }

    public void register(T dataIn)
    {
        if (dataIn.getSelfDmg() < fallBackDmg)
        {
            fallBack    = dataIn.getCrystal();
            fallBackDmg = dataIn.getSelfDmg();
        }

        this.data.add(dataIn);
    }

    public float getFallBackDmg()
    {
        return fallBackDmg;
    }

    public Entity getAntiTotem()
    {
        return antiTotem;
    }

    public void setAntiTotem(Entity antiTotem)
    {
        this.antiTotem = antiTotem;
    }

    public Entity getFallBack()
    {
        return fallBack;
    }

    public Collection<T> getData()
    {
        return data;
    }

}
