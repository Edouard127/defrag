package com.github.steveice10.opennbt.tag.builtin;

import com.google.common.base.Preconditions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A tag containing a string.
 */
public class StringTag extends Tag {
    public static final int ID = 8;
    private String value;

    /**
     * Creates a tag.
     */
    public StringTag() {
        this("");
    }

    /**
     * Creates a tag.
     *
     * @param value The value of the tag.
     */
    public StringTag(String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of this tag.
     *
     * @param value New value of this tag.
     */
    public void setValue(String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    @Override
    public void read(DataInput in) throws IOException {
        this.value = in.readUTF();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringTag stringTag = (StringTag) o;
        return this.value.equals(stringTag.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public final StringTag clone() {
        return new StringTag(this.value);
    }

    @Override
    public int getTagId() {
        return ID;
    }
}
