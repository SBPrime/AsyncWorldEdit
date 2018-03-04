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
package org.primesoft.asyncworldedit.changesetSerializer.serializers;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SBPrime
 */
public class NbtTagSerializer {

    public static final EndTag END_TAG = new EndTag();

    public final static int ID_EndTag = 0;
    public final static int ID_ByteTag = 1;
    public final static int ID_ShortTag = 2;
    public final static int ID_IntTag = 3;
    public final static int ID_LongTag = 4;
    public final static int ID_StringTag = 5;
    public final static int ID_FloatTag = 6;
    public final static int ID_DoubleTag = 7;
    public final static int ID_ByteArrayTag = 8;
    public final static int ID_IntArrayTag = 9;
    public final static int ID_ListTag = 10;
    public final static int ID_CompoundTag = 11;

    private final static Charset UTF8 = Charset.forName("UTF8");
    private final static Object[] DATA = new Object[]{
        ID_EndTag, EndTag.class,
        ID_ByteTag, ByteTag.class,
        ID_ShortTag, ShortTag.class,
        ID_IntTag, IntTag.class,
        ID_LongTag, LongTag.class,
        ID_StringTag, StringTag.class,
        ID_FloatTag, FloatTag.class,
        ID_DoubleTag, DoubleTag.class,
        ID_ByteArrayTag, ByteArrayTag.class,
        ID_IntArrayTag, IntArrayTag.class,
        ID_ListTag, ListTag.class,
        ID_CompoundTag, CompoundTag.class
    };
    private final static Map<Class<? extends Tag>, Integer> NBT2ID;
    private final static Map<Integer, Class<? extends Tag>> ID2NBT;

    static {
        NBT2ID = new LinkedHashMap<Class<? extends Tag>, Integer>();
        ID2NBT = new LinkedHashMap<Integer, Class<? extends Tag>>();

        for (int i = 0; i < DATA.length; i += 2) {
            Integer id = (Integer) DATA[i + 0];
            Class<? extends Tag> cls = (Class<? extends Tag>) DATA[i + 1];

            NBT2ID.put(cls, id);
            ID2NBT.put(id, cls);
        }
    }

    /**
     *
     * @param stream
     * @return
     * @throws IOException
     */
    public static Tag deserialize(DataInput stream) throws IOException {
        if (stream == null) {
            return null;
        }

        int id = stream.readInt();

        switch (id) {
            case ID_EndTag:
                return END_TAG;
            case ID_ByteTag:
                return new ByteTag(stream.readByte());
            case ID_ShortTag:
                return new ShortTag(stream.readShort());
            case ID_IntTag:
                return new IntTag(stream.readInt());
            case ID_LongTag:
                return new LongTag(stream.readLong());
            case ID_FloatTag:
                return new FloatTag(stream.readFloat());
            case ID_DoubleTag:
                return new DoubleTag(stream.readDouble());
            case ID_StringTag: {
                byte[] buf = new byte[stream.readInt()];
                stream.readFully(buf);

                return new StringTag(new String(buf, UTF8));
            }
            case ID_ByteArrayTag: {
                byte[] data = new byte[stream.readInt()];
                stream.readFully(data);

                return new ByteArrayTag(data);
            }
            case ID_IntArrayTag: {
                int count = stream.readInt();
                int[] data = new int[count];
                for (int i = 0; i < count; i++) {
                    data[i] = stream.readInt();
                }

                return new IntArrayTag(data);
            }
            case ID_ListTag: {
                Integer subType = stream.readInt();
                if (!ID2NBT.containsKey(subType)) {
                    return null;
                }

                Class<? extends Tag> subTypeCls = ID2NBT.get(subType);

                int count = stream.readInt();

                List<Tag> subTags = new ArrayList<Tag>(count);
                for (int i = 0; i < count; i++) {
                    Tag subTag = deserialize(stream);
                    if (subTag != null) {
                        subTags.add(subTag);
                    }
                }

                return new ListTag(subTypeCls, subTags);
            }
            case ID_CompoundTag: {
                int count = stream.readInt();
                Map<String, Tag> data = new LinkedHashMap<String, Tag>();

                for (int i = 0; i < count; i++) {
                    byte[] keyBuf = new byte[stream.readInt()];
                    stream.readFully(keyBuf);
                    String key = new String(keyBuf, UTF8);

                    Tag subTag = deserialize(stream);
                    if (subTag != null) {
                        data.put(key, subTag);
                    }
                }

                return new CompoundTag(data);
            }
        }
        return null;
    }

    /**
     * Serialize the Tag data
     *
     * @param stream
     * @param tag
     * @throws java.io.IOException
     */
    public static void serialize(DataOutput stream, Tag tag) throws IOException {
        if (tag == null) {
            return;
        }

        Class<?> tagClass = tag.getClass();

        if (tag instanceof EndTag) {
            stream.writeInt(ID_EndTag);

        } else if (tag instanceof ByteTag) {
            stream.writeInt(ID_ByteTag);

            stream.writeByte(((ByteTag) tag).getValue());
        } else if (tag instanceof ShortTag) {
            stream.writeInt(ID_ShortTag);

            stream.writeShort(((ShortTag) tag).getValue());
        } else if (tag instanceof IntTag) {
            stream.writeInt(ID_IntTag);

            stream.writeInt(((IntTag) tag).getValue());
        } else if (tag instanceof LongTag) {
            stream.writeInt(ID_LongTag);

            stream.writeLong(((LongTag) tag).getValue());
        } else if (tag instanceof FloatTag) {
            stream.writeInt(ID_FloatTag);

            stream.writeFloat(((FloatTag) tag).getValue());
        } else if (tag instanceof DoubleTag) {
            stream.writeInt(ID_DoubleTag);

            stream.writeDouble(((DoubleTag) tag).getValue());
        } else if (tag instanceof StringTag) {
            stream.writeInt(ID_StringTag);

            String value = ((StringTag) tag).getValue();
            byte[] buf = value.getBytes(UTF8);
            stream.writeInt(buf.length);
            stream.write(buf);
        } else if (tag instanceof ByteArrayTag) {
            stream.writeInt(ID_ByteArrayTag);

            byte[] data = ((ByteArrayTag) tag).getValue();
            stream.writeInt(data.length);
            stream.write(data);
        } else if (tag instanceof IntArrayTag) {
            stream.writeInt(ID_IntArrayTag);

            int[] data = ((IntArrayTag) tag).getValue();
            stream.writeInt(data.length);
            for (int i : data) {
                stream.writeInt(i);
            }
        } else if (tag instanceof ListTag) {
            stream.writeInt(ID_ListTag);

            ListTag list = (ListTag) tag;
            Class<?> type = list.getType();
            List<Tag> value = list.getValue();

            Integer typeId = NBT2ID.get(type);
            if (typeId == null) {
                return;
            }
            

            stream.writeInt(typeId);
            stream.writeInt(value.size());

            for (Tag subTag : value) {
                serialize(stream, subTag);
            }
        } else if (tag instanceof CompoundTag) {
            stream.writeInt(ID_CompoundTag);

            Map<String, Tag> data = ((CompoundTag) tag).getValue();
            stream.writeInt(data.size());
            for (String key : data.keySet()) {
                Tag value = data.get(key);
                byte[] keyBuf = key.getBytes(UTF8);
                stream.writeInt(keyBuf.length);
                stream.write(keyBuf);

                serialize(stream, value);
            }
        }
    }
}
