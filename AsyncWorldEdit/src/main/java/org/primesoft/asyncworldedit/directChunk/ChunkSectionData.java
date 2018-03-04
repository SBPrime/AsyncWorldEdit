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
package org.primesoft.asyncworldedit.directChunk;

import org.primesoft.asyncworldedit.api.directChunk.IChunkSection;

/**
 *
 * @author SBPrime
 */
public class ChunkSectionData implements IChunkSection {
    private final int m_y;
    
    private final char[] m_blockIds;
    
    private final byte[] m_emittedLight;
    
    private final byte[] m_skyLight;
    
    /**
     * Create new instance of chunk section data
     * @param y
     * @param blockIds
     * @param skyLight
     */
    public ChunkSectionData(int y, char[] blockIds, boolean skyLight) {
        this(y, blockIds, null, skyLight ? new byte[2048] : null);
    }
    
    /**
     * Create new instance of chunk section data
     * @param y
     * @param blockIds
     * @param emittedLight
     * @param skylight
     */
    public ChunkSectionData(int y, char[] blockIds, byte[] emittedLight, byte[] skylight){
        m_y = y;
        
        if (blockIds.length != 4096) {
            throw new IllegalArgumentException(String.format("BlockIds should be 4096 bytes not: %1$s", blockIds.length));
        }
        
        if (emittedLight == null) {
            emittedLight = DcUtils.newSectionEmittedLight();
        }
        
        if (emittedLight.length != 2048) {
            throw new IllegalArgumentException(String.format("EmittedLight should be 2048 bytes not: %1$s", emittedLight.length));
        }
        
        if (skylight != null && skylight.length != 2048){
            throw new IllegalArgumentException(String.format("Skylight should be 2048 bytes not: %1$s", skylight.length));
        }
        
        m_blockIds = blockIds;
        m_emittedLight = emittedLight;
        m_skyLight = skylight;
    }
    
    @Override
    public char[] getBlockIds() {
        return m_blockIds;
    }
    
    @Override
    public byte[] getSkyLight() {
        return m_skyLight;
    }
    
    @Override
    public byte[] getEmittedLight() {
        return m_emittedLight;
    }
    
    @Override
    public int getY() {
        return m_y;
    }
}
