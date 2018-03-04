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
package org.primesoft.asyncworldedit.directChunk.entries;

import org.primesoft.asyncworldedit.api.directChunk.IBlockEntry;
import com.sk89q.jnbt.CompoundTag;

/**
 *
 * @author SBPrime
 */
public class BlockEntry extends VectorEntry implements IBlockEntry {

    /**
     * Block native ID
     */
    private final char m_id;

    /**
     * The NBT data
     */
    private final CompoundTag m_tag;

    /**
     * The light emission level
     */
    private byte m_emissionLevel;
    
    /**
     * Contains block data
     */
    private final boolean m_hasBlockData;
    
    /**
     * The sky light level
     */
    private byte m_skyLevel;

    /**
     * Get the block ID
     *
     * @return
     */
    @Override
    public char getId() {
        return m_id;
    }

    /**
     * Get the NBT data
     *
     * @return
     */
    @Override
    public CompoundTag getNbt() {
        return m_tag;
    }

    /**
     * Get the light emission level
     *
     * @return
     */
    @Override
    public byte getEmission() {
        return m_emissionLevel;
    }

    /**
     * Set the light emission level
     *
     * @param level
     */
    @Override
    public void setEmission(byte level) {
        m_emissionLevel = level;
    }
    
    /**
     * Get the sky light level
     *
     * @return
     */
    @Override
    public byte getSky() {
        return m_skyLevel;
    }

    /**
     * Set the sky light level
     *
     * @param level
     */
    @Override
    public void setSky(byte level) {
        m_skyLevel = level;
    }

    @Override
    public boolean hasBlock() {
        return m_hasBlockData;
    }

    @Override
    public boolean hasLight() {
        return m_emissionLevel > -1;
    }
    
    @Override
    public boolean hasSkyLight() {
        return m_skyLevel > -1;
    }
    
    /**
     * Create new instance of Block entry
     *
     * @param id
     * @param x
     * @param y
     * @param z
     * @param nbt
     * @param emissionLevel
     */
    private BlockEntry(char id, int x, int y, int z, CompoundTag nbt, 
            byte emissionLevel, byte skyLevel, boolean hasBlockData) {
        super(x, y, z);

        m_id = id;
        m_tag = nbt;
        m_emissionLevel = emissionLevel;
        m_skyLevel = skyLevel;
        
        m_hasBlockData = hasBlockData;        
    }
    
    /**
     * Create new instance of Block entry
     *
     * @param id
     * @param x
     * @param y
     * @param z
     * @param nbt
     * @param emissionLevel
     */
    public BlockEntry(char id, int x, int y, int z, CompoundTag nbt, byte emissionLevel) {
        this(id, x, y, z, nbt, emissionLevel, (byte)-1, true);
    }
    
    /**
     * Create new instance of Block entry
     *
     * @param id
     * @param x
     * @param y
     * @param z
     * @param nbt
     * @param emissionLevel
     * @param skyLevel
     */
    public BlockEntry(char id, int x, int y, int z, CompoundTag nbt, byte emissionLevel, byte skyLevel) {
        this(id, x, y, z, nbt, emissionLevel, skyLevel, true);
    }

    /**
     * Create new instance of Block entry
     *
     * @param id
     * @param x
     * @param y
     * @param z
     * @param nbt
     */
    public BlockEntry(char id, int x, int y, int z, CompoundTag nbt) {
        this(id, x, y, z, nbt, (byte)-1, (byte)-1, true);
    }
    
    /**
     * Create new instance of Block entry
     *
     * @param x
     * @param y
     * @param z
     * @param emissionLevel
     */
    public BlockEntry(int x, int y, int z, byte emissionLevel) {
        this((char)-1, x, y, z, null, emissionLevel, (byte)-1, false);
    }
    
    /**
     * Create new instance of Block entry
     *
     * @param x
     * @param y
     * @param z
     * @param emissionLevel
     * @param skyLevel
     */
    public BlockEntry(int x, int y, int z, byte emissionLevel, byte skyLevel) {
        this((char)-1, x, y, z, null, emissionLevel, skyLevel, false);
    }
}
