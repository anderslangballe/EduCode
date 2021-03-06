package com.educode.runtime.types;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * Created by zen on 4/24/17.
 */
public class MinecraftItem implements IItem
{
    private final ItemStack _wrappedItem;

    public MinecraftItem()
    {
        this._wrappedItem = new ItemStack(Blocks.AIR);
    }

    public MinecraftItem(ItemStack item)
    {
        this._wrappedItem = item;
    }

    public ItemStack getWrappedItem()
    {
        return this._wrappedItem;
    }

    @Override
    public String toString()
    {
        return this.getWrappedItem().getDisplayName();
    }

    @Override
    public double getCount()
    {
        return this.getWrappedItem().getCount();
    }

    @Override
    public String getName()
    {
        return this.getWrappedItem().getDisplayName();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MinecraftItem)
        {
            MinecraftItem other = (MinecraftItem) obj;

            return this.getWrappedItem().equals(other.getWrappedItem());
        }

        return false;
    }
}
