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
package org.primesoft.asyncworldedit.adapter.spigot_v1_9_R2.directChunk;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTReadLimiter;
import net.minecraft.server.v1_9_R2.NBTTagByte;
import net.minecraft.server.v1_9_R2.NBTTagByteArray;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagDouble;
import net.minecraft.server.v1_9_R2.NBTTagEnd;
import net.minecraft.server.v1_9_R2.NBTTagFloat;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagIntArray;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagLong;
import net.minecraft.server.v1_9_R2.NBTTagShort;
import net.minecraft.server.v1_9_R2.NBTTagString;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.World;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedTileEntity;
import org.primesoft.asyncworldedit.directChunk.NBTType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class Nbt {

    private static final NBTReadLimiter s_limiter = NBTReadLimiter.a;

    private static final Method s_mWrite;
    private static final Method s_mLoad;

    private static final Constructor<NBTTagEnd> s_tagEnd;

    private static final boolean s_initialized;
    
    public static boolean isInitialized() {
        return s_initialized;
    }

    static {
        Class<?> cNbtTagCompound = NBTTagCompound.class;

        s_mLoad = Reflection.findMethod(cNbtTagCompound, "load", "Unable to find load method",
                DataInput.class, int.class, NBTReadLimiter.class);
        s_mWrite = Reflection.findMethod(cNbtTagCompound, "write", "Unable to find write method",
                DataOutput.class);

        s_tagEnd = (Constructor<NBTTagEnd>) Reflection.findConstructor(NBTTagEnd.class, "Unable to get NBTTagEnd constructor");

        s_initialized = s_mLoad != null && s_mWrite != null && s_tagEnd != null;
    }

    static CompoundTag convertTag(NBTTagCompound mcTag) {
        return (CompoundTag) convertTag((NBTBase) mcTag);
    }

    static NBTTagCompound convertTag(CompoundTag weTag) {
        return (NBTTagCompound) convertTag((Tag) weTag);
    }

    private static Tag convertTag(NBTBase nbt) {
        if (nbt == null || !s_initialized) {
            return null;
        }

        /**
         * Convert the complex tag
         */
        if ((nbt instanceof NBTTagCompound)) {
            HashMap<String, Tag> childTags = new HashMap<String, Tag>();
            for (String key : ((NBTTagCompound) nbt).c()) {
                Tag subTag = convertTag(((NBTTagCompound) nbt).get(key));
                if (subTag != null) {
                    childTags.put(key, subTag);
                }
            }
            return new CompoundTag(childTags);
        }

        /**
         * Convert primitives
         */
        if ((nbt instanceof NBTTagByte)) {
            return new ByteTag(((NBTTagByte) nbt).f());
        }
        if ((nbt instanceof NBTTagShort)) {
            return new ShortTag(((NBTTagShort) nbt).e());
        }
        if ((nbt instanceof NBTTagInt)) {
            return new IntTag(((NBTTagInt) nbt).d());
        }
        if ((nbt instanceof NBTTagLong)) {
            return new LongTag(((NBTTagLong) nbt).c());
        }
        if ((nbt instanceof NBTTagFloat)) {
            return new FloatTag(((NBTTagFloat) nbt).h());
        }
        if ((nbt instanceof NBTTagDouble)) {
            return new DoubleTag(((NBTTagDouble) nbt).g());
        }
        if ((nbt instanceof NBTTagString)) {
            return new StringTag(((NBTTagString) nbt).a_());
        }

        /**
         * Convert arrays
         */
        if ((nbt instanceof NBTTagByteArray)) {
            return new ByteArrayTag(((NBTTagByteArray) nbt).c());
        }

        if ((nbt instanceof NBTTagIntArray)) {
            return new IntArrayTag(((NBTTagIntArray) nbt).c());
        }
        if ((nbt instanceof NBTTagList)) {
            NBTTagList nbtList = (NBTTagList) nbt;
            List<Tag> values = new ArrayList<Tag>();
            Class<?> cls = null;

            for (int idx = 0; idx < nbtList.size(); idx++) {
                NBTBase nbtEntry = nbtList.h(idx);
                if (nbtEntry instanceof NBTTagEnd) {
                    continue;
                }

                Tag entry = convertTag(nbtEntry);

                if (entry == null) {
                    continue;
                }

                if (cls == null) {
                    cls = entry.getClass();
                } else if (cls != entry.getClass()) {
                    log(String.format("NBTTagList contains multiple types of NBTTgs. Current: %1$s New: %2$s",
                            cls.getCanonicalName(), entry.getClass().getCanonicalName()));
                    continue;
                }

                values.add(entry);
            }

            //Class cls = NBTConstants.getClassFromType(nbtList.f());
            return new ListTag((Class<? extends Tag>) cls, values);
        }

        if ((nbt instanceof NBTTagEnd)) {
            return new EndTag();
        }

        log(String.format("Unknown NMS %1$s... skipping.", nbt.getClass().getCanonicalName()));
        return null;
    }

    private static NBTBase convertTag(Tag tag) {
        if (tag == null || !s_initialized) {
            return null;
        }

        /**
         * Convert the complex tag
         */
        if ((tag instanceof CompoundTag)) {
            NBTTagCompound nbt = new NBTTagCompound();
            for (Map.Entry<String, Tag> entry : ((CompoundTag) tag).getValue().entrySet()) {
                NBTBase subNbt = convertTag(entry.getValue());
                if (subNbt != null) {
                    nbt.set(entry.getKey(), subNbt);
                }
            }
            return nbt;
        }

        /**
         * Convert primitives
         */
        if ((tag instanceof ByteTag)) {
            return new NBTTagByte(((ByteTag) tag).getValue());
        }
        if ((tag instanceof ShortTag)) {
            return new NBTTagShort(((ShortTag) tag).getValue());
        }
        if ((tag instanceof IntTag)) {
            return new NBTTagInt(((IntTag) tag).getValue());
        }
        if ((tag instanceof LongTag)) {
            return new NBTTagLong(((LongTag) tag).getValue());
        }
        if ((tag instanceof FloatTag)) {
            return new NBTTagFloat(((FloatTag) tag).getValue());
        }
        if ((tag instanceof DoubleTag)) {
            return new NBTTagDouble(((DoubleTag) tag).getValue());
        }
        if ((tag instanceof StringTag)) {
            return new NBTTagString(((StringTag) tag).getValue());
        }

        /**
         * Convert arrays
         */
        if ((tag instanceof ByteArrayTag)) {
            return new NBTTagByteArray(((ByteArrayTag) tag).getValue());
        }

        if ((tag instanceof IntArrayTag)) {
            return new NBTTagIntArray(((IntArrayTag) tag).getValue());
        }
        if ((tag instanceof ListTag)) {
            NBTTagList nbt = new NBTTagList();
            ListTag foreignList = (ListTag) tag;
            for (Tag t : foreignList.getValue()) {
                nbt.add(convertTag(t));
            }
            return nbt;
        }

        if ((tag instanceof EndTag)) {
            return Reflection.create(NBTTagEnd.class, s_tagEnd, "Unable to create the NBTTagEnd");
        }

        log(String.format("Unknown NMS %1$s... skipping.", tag.getClass().getCanonicalName()));
        return null;
    }

    /**
     * Serialise the NBT data
     *
     * @param nbt
     * @return
     */
    static byte[] serialise(NBTTagCompound nbt) {
        if (!s_initialized) {
            return null;
        }
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(bs);
        if (!Reflection.invoke(nbt, s_mWrite, "Unable to write NTB data to stream",
                out)) {
            return null;
        }

        return bs.toByteArray();
    }

    /**
     * Deserialise the NBT data
     *
     * @param data
     * @return
     */
    static NBTTagCompound deserialise(byte[] data) {
        ByteArrayInputStream bs = new ByteArrayInputStream(data);
        DataInput in = new DataInputStream(bs);
        NBTTagCompound nbt = new NBTTagCompound();
        Reflection.invoke(nbt, s_mLoad, "Unable to read NTB data from stream",
                in, (int) 0, s_limiter);

        if (!s_initialized) {
            return null;
        }

        return nbt;
    }

    /**
     * serialise the tiled entities
     *
     * @param tileEntities
     * @return
     */
    static ISerializedTileEntity[] serialise(int cx, int cz, Collection<TileEntity> tileEntities) {
        if (!s_initialized) {
            return null;
        }

        final List<ISerializedTileEntity> result = new ArrayList<ISerializedTileEntity>();

        if (tileEntities != null) {
            for (TileEntity te : tileEntities) {
                result.add(new SerializedTileEntity(cx, cz, te));
            }
        }
        return result.toArray(new ISerializedTileEntity[0]);
    }

    /**
     * deserialise the tiled entities
     *
     * @param tileEntities
     * @return
     */
    static TileEntity[] deserialise(int cx, int cz, ISerializedTileEntity[] data) {
        if (!s_initialized) {
            return null;
        }

        final List<TileEntity> result = new LinkedList<TileEntity>();

        if (data != null) {
            for (ISerializedTileEntity entry : data) {
                SerializedTileEntity sEntry = entry instanceof SerializedTileEntity ? (SerializedTileEntity) entry : null;
                if (sEntry == null) {
                    continue;
                }

                result.add(sEntry.getTileEntity(cx, cz));
            }
        }

        return result.toArray(new TileEntity[0]);
    }

    /**
     * Serialise the entities
     *
     * @param entity
     * @return
     */
    static ISerializedEntity[] serialise(int cx, int cz, List<Entity>[] entity) {
        if (!s_initialized) {
            return null;
        }

        final List<ISerializedEntity> result = new ArrayList<ISerializedEntity>();

        for (List<Entity> slice : entity) {
            for (Entity e : slice) {
                if (e instanceof EntityPlayer) {
                    //Skip player entity
                    continue;
                }
                
                //NOTE: This might require changes
                //Looks like in 1.9 it stores entities 
                //from vehicle to passanger
                if (e.dead || !e.passengers.isEmpty()) {
                    continue;
                }

                try {
                    SerializedEntity sEntity = null;
                    do {
                        SerializedEntity passenger = sEntity;
                        sEntity = new SerializedEntity(cx, cz, e);

                        if (passenger == null) {
                            result.add(sEntity);
                        } else {
                            passenger.setVehicle(sEntity);
                        }

                        //Get the vehicle
                        e = e.bz();
                    } while (e != null);
                } catch (IllegalArgumentException ex) {
                    ExceptionHelper.printException(ex, String.format("Unable to serialise entity %1$s", e));
                }
            }
        }

        return result.toArray(new ISerializedEntity[0]);
    }

    /**
     * deserialise the entities
     *
     * @return
     */
    static Entity[] deserialise(int cx, int cz, World world, ISerializedEntity[] data) {
        if (!s_initialized) {
            return null;
        }

        final List<Entity> result = new LinkedList<Entity>();

        if (data != null) {
            for (ISerializedEntity entry : data) {
                SerializedEntity sEntry = entry instanceof SerializedEntity ? (SerializedEntity) entry : null;
                if (sEntry == null) {
                    log(String.format("Unsupported entity type: %1$s", entry.getClass().getCanonicalName()));
                    continue;
                }

                Entity e = sEntry.getEntity(cx, cz, world);
                if (e != null) {
                    result.add(e);
                } else {
                    log("Unable to deserialise entity data.");
                }
            }
        }

        return result.toArray(new Entity[0]);
    }

    /**
     * Extract UUID from NBT tag
     *
     * @param nbt
     * @return
     */
    public static UUID getUUID(NBTTagCompound nbt) {
        UUID result;

        if (nbt == null) {
            result = UUID.randomUUID();
        } else if (nbt.hasKeyOfType("UUIDMost", NBTType.NBT_LONG.getId()) && nbt.hasKeyOfType("UUIDLeast", NBTType.NBT_LONG.getId())) {
            result = new UUID(nbt.getLong("UUIDMost"), nbt.getLong("UUIDLeast"));
        } else if (nbt.hasKeyOfType("UUID", NBTType.NBT_STRING.getId())) {
            result = UUID.fromString(nbt.getString("UUID"));
        } else {
            result = UUID.randomUUID();
        }

        return result;
    }
}
