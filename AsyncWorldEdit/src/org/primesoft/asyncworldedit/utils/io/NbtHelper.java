/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.utils.io;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.util.Location;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SBPrime
 */
public final class NbtHelper {

    public static Tag createVector(Vector vector) {
        List<DoubleTag> list = new ArrayList<DoubleTag>();
        list.add(new DoubleTag(vector.getX()));
        list.add(new DoubleTag(vector.getY()));
        list.add(new DoubleTag(vector.getZ()));
        return new ListTag(DoubleTag.class, list);
    }

    public static Tag createRotation(Location location) {
        List<FloatTag> list = new ArrayList<FloatTag>();
        list.add(new FloatTag(location.getYaw()));
        list.add(new FloatTag(location.getPitch()));
        return new ListTag(FloatTag.class, list);
    }

    public static <T extends Tag> T getTag(Class<T> cls, Map<String, Tag> nbt, String key) throws IOException {
        return getTag(cls, nbt, key, false);
    }

    public static <T extends Tag> T getTag(Class<T> cls, Map<String, Tag> nbt, String key, boolean optional) throws IOException {
        if (!nbt.containsKey(key)) {
            if (optional) {
                return null;
            }
            throw new IOException(String.format("Missing TAG \"%1$s\"", key));
        }

        Tag tag = nbt.get(key);
        if (!cls.isInstance(tag)) {
            throw new IOException(String.format("Invalid TAG type. Expected: %1$s, Got: %2$s",
                    cls.getName(), tag.getClass().getName()));
        }

        return cls.cast(tag);
    }

    public static <T extends Tag> T getOptionalTag(Class<T> cls, Map<String, Tag> nbt, String key) throws IOException {
        return getTag(cls, nbt, key, true);
    }

    public static ListTag getListTag(Map<String, Tag> nbt, String key) throws IOException {
        return getTag(ListTag.class, nbt, key);
    }
    
    public static ListTag getListTag(Map<String, Tag> nbt, String key, boolean optional) throws IOException {
        return getTag(ListTag.class, nbt, key, optional);
    }
    
    public static StringTag getStringTag(Map<String, Tag> nbt, String key) throws IOException {
        return getTag(StringTag.class, nbt, key);
    }

    public static ByteArrayTag getByteArrayTag(Map<String, Tag> nbt, String key) throws IOException {
        return getTag(ByteArrayTag.class, nbt, key);
    }

    public static ByteArrayTag getByteArrayTag(Map<String, Tag> nbt, String key, boolean optional) throws IOException {
        return getTag(ByteArrayTag.class, nbt, key, optional);
    }
    
    public static IntArrayTag getIntArrayTag(Map<String, Tag> nbt, String key, boolean optional) throws IOException {
        return getTag(IntArrayTag.class, nbt, key, optional);
    }

    public static IntTag getIntTag(Map<String, Tag> nbt, String key) throws IOException {
        return getTag(IntTag.class, nbt, key);
    }

    public static IntTag getIntTag(Map<String, Tag> nbt, String key, boolean optional) throws IOException {
        return getTag(IntTag.class, nbt, key, optional);
    }

    public static ShortTag getShortTag(Map<String, Tag> nbt, String key) throws IOException {
        return getTag(ShortTag.class, nbt, key);
    }

    public static String getString(Map<String, Tag> nbt, String key) throws IOException {
        return getStringTag(nbt, key).getValue();
    }

    public static int getInt(Map<String, Tag> nbt, String key) throws IOException {
        return getIntTag(nbt, key).getValue();
    }
    
    public static byte[] getByteArray(Map<String, Tag> nbt, String key) throws IOException {
        return getByteArrayTag(nbt, key).getValue();
    }
    
    public static byte[] getByteArray(Map<String, Tag> nbt, String key, byte[] defaultValue) throws IOException {
        ByteArrayTag tag = getByteArrayTag(nbt, key, true);
        
        if (tag == null) {
            return defaultValue;
        }

        return tag.getValue();
    }
    
    public static int[] getIntArray(Map<String, Tag> nbt, String key, int[] defaultValue) throws IOException {
        IntArrayTag tag = getIntArrayTag(nbt, key, true);
        
        if (tag == null) {
            return defaultValue;
        }

        return tag.getValue();
    }

    public static int getInt(Map<String, Tag> nbt, String key, int defaultValue) throws IOException {
        IntTag tag = getIntTag(nbt, key, true);

        if (tag == null) {
            return defaultValue;
        }

        return tag.getValue();
    }

    public static short getShort(Map<String, Tag> nbt, String key) throws IOException {
        return getShortTag(nbt, key).getValue();
    }
    
    public static List<Tag> getList(Map<String, Tag> nbt, String key) throws IOException {
        return getListTag(nbt, key).getValue();
    }
    
    
    public static List<Tag> getList(Map<String, Tag> nbt, String key, List<Tag> defaultValue) throws IOException {
        ListTag tag = getListTag(nbt, key, true);

        if (tag == null) {
            return defaultValue;
        }

        return tag.getValue();
    }
}
