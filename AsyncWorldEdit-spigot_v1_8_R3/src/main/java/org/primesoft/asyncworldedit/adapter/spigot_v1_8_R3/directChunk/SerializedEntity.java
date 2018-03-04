/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.adapter.spigot_v1_8_R3.directChunk;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.internal.helper.MCDirections;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.World;
import static org.primesoft.asyncworldedit.adapter.spigot_v1_8_R3.directChunk.Nbt.deserialise;
import org.primesoft.asyncworldedit.directChunk.NBTType;
import org.primesoft.asyncworldedit.directChunk.base.BaseSerializedEntity;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
class SerializedEntity extends BaseSerializedEntity {

    SerializedEntity(Vector position, float yaw, float pitch, NBTTagCompound nbt) {
        super(Nbt.getUUID(nbt), position, yaw, pitch, Nbt.serialise(nbt));
    }

    SerializedEntity(UUID uuid, Vector position, float yaw, float pitch,
            byte[] nbt) {
        super(uuid, position, yaw, pitch, nbt);
    }

    SerializedEntity(int cx, int cz, Entity e) throws IllegalArgumentException {
        this(cx, cz, extractNbt(e));
    }

    /**
     * Extract NBT from entity
     *
     * @param e
     */
    private static NBTTagCompound extractNbt(Entity e) throws IllegalArgumentException {
        NBTTagCompound nbt = new NBTTagCompound();
        if (!e.c(nbt)) {
            throw new IllegalArgumentException("Unable to get NBT data");
        }

        if (!nbt.hasKey("Pos")) {
            throw new IllegalArgumentException("No position in NBT data");
        }

        if (nbt.hasKey("Riding")) {
            nbt.remove("Riding");
        }

        return nbt;
    }

    private SerializedEntity(int cx, int cz, NBTTagCompound nbt) throws IllegalArgumentException {
        this(cx, cz, 
                nbt.hasKey("Rotation") ? nbt.getList("Rotation", NBTType.NBT_FLOAT.getId()) : null,
                nbt.getList("Pos", NBTType.NBT_DOUBLE.getId()), nbt);
    }
    
    private SerializedEntity(int cx, int cz,
            NBTTagList rotation, NBTTagList position,
            NBTTagCompound nbt) throws IllegalArgumentException {
        super(Nbt.getUUID(nbt), 
                new Vector(position.d(0) - (16 * cx), position.d(1), position.d(2) - (16 * cz)),
                rotation != null ? rotation.e(0) : Float.NaN,
                rotation != null ? rotation.e(1) : Float.NaN,
                Nbt.serialise(nbt));
    }

    /**
     * Get the possition corrected NBT data
     *
     * @param cx
     * @param cz
     * @return
     */
    private NBTTagCompound getNBT(int cx, int cz) {
        Vector pos = m_postion;
        NBTTagCompound nbt = deserialise(m_data);

        double px = pos.getX() + 16 * cx;
        double py = pos.getY();
        double pz = pos.getZ() + 16 * cz;

        double ox, oy, oz;

        //Fix Position
        if (nbt.hasKey("Pos")) {
            NBTTagList position = nbt.getList("Pos", NBTType.NBT_DOUBLE.getId());
            ox = position.d(0);
            oy = position.d(1);
            oz = position.d(2);

            position.a(0, new NBTTagDouble(px));
            position.a(1, new NBTTagDouble(py));
            position.a(2, new NBTTagDouble(pz));
        } else {
            ox = px;
            oy = py;
            oz = pz;
        }

        //Fix tile
        if (nbt.hasKey("xTile")) {
            nbt.setShort("xTile", (short) (PositionHelper.positionToBlockPosition(px) + nbt.getShort("xTile") - PositionHelper.positionToBlockPosition(ox)));
        }
        if (nbt.hasKey("yTile")) {
            nbt.setShort("yTile", (short) (PositionHelper.positionToBlockPosition(py) + nbt.getShort("yTile") - PositionHelper.positionToBlockPosition(oy)));
        }
        if (nbt.hasKey("zTile")) {
            nbt.setShort("zTile", (short) (PositionHelper.positionToBlockPosition(pz) + nbt.getShort("zTile") - PositionHelper.positionToBlockPosition(oz)));
        }

        if (nbt.hasKey("TileX")) {
            nbt.setShort("TileX", (short) (PositionHelper.positionToBlockPosition(px) + nbt.getShort("TileX") - PositionHelper.positionToBlockPosition(ox)));
        }
        if (nbt.hasKey("TileY")) {
            nbt.setShort("TileY", (short) (PositionHelper.positionToBlockPosition(py) + nbt.getShort("TileY") - PositionHelper.positionToBlockPosition(oy)));
        }
        if (nbt.hasKey("TileZ")) {
            nbt.setShort("TileZ", (short) (PositionHelper.positionToBlockPosition(pz) + nbt.getShort("TileZ") - PositionHelper.positionToBlockPosition(oz)));
        }

        //Fix leasch               
        //if (nbt.hasKey("Leash")) {
        //    NBTTagCompound leash = nbt.getCompound("Leash");
        //    int x = leash.getInt("X") % 16 + (cx * 16);
        //    int z = leash.getInt("Z") % 16 + (cz * 16);
        //    leash.setInt("X", x);
        //    leash.setInt("Z", z);
        //}
        //Fix UUID
        nbt.remove("UUIDMost");
        nbt.remove("UUIDLeast");
        nbt.remove("UUID");
        //We do not need to provide new UUID its going to be set in 
        //the constructor

        //Fix World UUID
        nbt.remove("world");
        nbt.remove("WorldUUIDMost");
        nbt.remove("WorldUUIDLeast");
        //We do not need to provide the world UUID its going to be
        //read from the world in create

        //Set the new YAW & PITCH
        float yaw = m_yaw;
        float pitch = m_pitch;

        yaw -= 360 * (int) (yaw / 360);
        if (yaw < 0) {
            yaw += 360;
        }

        if (pitch < -90.0f) {
            pitch = -90.0f;
        } else if (pitch > 90.0f) {
            pitch = 90.0f;
        }

        if (nbt.hasKey("Rotation")
                && !Float.isNaN(m_yaw) && !Float.isNaN(m_pitch)) {
            NBTTagList rotation = nbt.getList("Rotation", NBTType.NBT_FLOAT.getId());

            rotation.a(0, new NBTTagFloat(yaw));
            rotation.a(1, new NBTTagFloat(pitch));
        }

        if (nbt.hasKey("Facing")) {
            byte oldFacing = nbt.getByte("Facing");
            byte newFacing = (byte) (yaw / 90);

            if (oldFacing != newFacing) {
                nbt.setByte("Facing", newFacing);
            }
        }

        boolean hasDirection = nbt.hasKey("Direction");
        boolean hasDir = nbt.hasKey("Dir");
        if (hasDir || hasDirection) {
            byte mcDirection = (byte) MCDirections.toHanging(findDirection(yaw));

            if (hasDirection) {
                nbt.setByte("Direction", mcDirection);
            }
            if (hasDir) {
                nbt.setByte("Dir", MCDirections.toLegacyHanging(mcDirection));
            }
        }

        return nbt;
    }

    /**
     * Create a native entity
     *
     * @param cx
     * @param cz
     * @param world
     * @return
     */
    Entity getEntity(int cx, int cz, World world) {
        return EntityTypes.a(getNBT(cx, cz), world);
    }

    /**
     * Get the raw NBT data
     *
     * @param cx Destination chunk X
     * @param cz Destionation chunk Z
     * @param newUuid New uuid to assign to the entity
     * @return
     */
    @Override
    public CompoundTag getRawData(int cx, int cz, UUID newUuid) {
        NBTTagCompound nbt = getNBT(cx, cz);

        if (newUuid != null) {
            nbt.setLong("UUIDMost", newUuid.getMostSignificantBits());
            nbt.setLong("UUIDLeast", newUuid.getLeastSignificantBits());
        }

        return Nbt.convertTag(nbt);
    }
}
