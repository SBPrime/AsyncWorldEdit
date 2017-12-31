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
import com.sk89q.worldedit.BlockVector;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntity;
import static org.primesoft.asyncworldedit.adapter.spigot_v1_8_R3.directChunk.Nbt.deserialise;
import static org.primesoft.asyncworldedit.adapter.spigot_v1_8_R3.directChunk.Nbt.serialise;
import org.primesoft.asyncworldedit.directChunk.base.BaseSerializedTileEntity;

/**
 *
 * @author SBPrime
 */
class SerializedTileEntity extends BaseSerializedTileEntity {

    SerializedTileEntity(BlockVector position, byte[] data) {
        super(position, data);
    }

    SerializedTileEntity(int cx, int cz, TileEntity te) throws IllegalArgumentException {
        this(cx, cz, extractNbt(te));
    }

    /**
     * Extract NBT from entity
     *
     * @param e
     */
    private static NBTTagCompound extractNbt(TileEntity te) throws IllegalArgumentException {
        NBTTagCompound nbt = new NBTTagCompound();
        te.b(nbt);
        return nbt;
    }

    private SerializedTileEntity(int cx, int cz, NBTTagCompound nbt) {
        super(new BlockVector(nbt.getInt("x") - (16 * cx),
                nbt.getInt("y"),
                nbt.getInt("z") - (16 * cz)), serialise(nbt));
    }

    TileEntity getTileEntity(int cx, int cz) {

        BlockVector pos = m_postion;
        NBTTagCompound nbt = deserialise(m_data);

        nbt.setInt("x", (16 * cx) + pos.getBlockX());
        nbt.setInt("y", pos.getBlockY());
        nbt.setInt("z", (16 * cz) + pos.getBlockZ());
        return TileEntity.c(nbt);
    }

    /**
     * Get the raw NBT data
     *
     * @return
     */
    @Override
    public CompoundTag getRawData(int cx, int cz) {
        BlockVector pos = m_postion;
        NBTTagCompound nbt = deserialise(m_data);

        nbt.setInt("x", (16 * cx) + pos.getBlockX());
        nbt.setInt("y", pos.getBlockY());
        nbt.setInt("z", (16 * cz) + pos.getBlockZ());

        return Nbt.convertTag(nbt);
    }
}
