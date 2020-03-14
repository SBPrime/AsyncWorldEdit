/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.primesoft.asyncworldedit.api.blockPlacer.ICountProvider;

/**
 *
 * @author prime
 */
public class ChunkBaseRegionIterator implements Iterator<BlockVector3>, ICountProvider {

    private final int m_minX, m_minY, m_minZ;
    private final int m_maxX, m_maxY, m_maxZ;
    private int m_x, m_y, m_z;
    private int m_xChunk, m_zChunk;
    private final AbstractRegion m_region;
    private int m_count;
    private AtomicInteger m_delta = new AtomicInteger(0);

    public ChunkBaseRegionIterator(AbstractRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        m_minX = min.getBlockX();
        m_minY = min.getBlockY();
        m_minZ = min.getBlockZ();

        m_x = min.getBlockX();
        m_y = min.getBlockY();
        m_z = min.getBlockZ();

        m_xChunk = getChunk(m_x);
        m_zChunk = getChunk(m_z);

        m_maxX = max.getBlockX();
        m_maxY = max.getBlockY();
        m_maxZ = max.getBlockZ();
        
        m_region = region;
    
        m_count = (m_maxX - m_minX + 1) * (m_maxY - m_minY + 1) * (m_maxZ - m_minZ + 1);
        forward();
    }
    
    private void forward() {
        while (hasNext() && !m_region.contains(BlockVector3.at(m_x, m_y, m_z))) {
            forwardOne();
        }
    }
    
    private void forwardOne() {
        m_y++;
        if (m_y > m_maxY) {
            m_y = m_minY;

            incXZ();
        }
        
        m_count--;
        m_delta.incrementAndGet();
    }

    @Override
    public boolean hasNext() {
        return m_z <= m_maxZ;
    }

    @Override
    public BlockVector3 next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final BlockVector3 result = BlockVector3.at(m_x, m_y, m_z);
        forwardOne();
        forward();

        return result;
    }
    
    private void incXZ() {
        m_x++;
        if (m_x > m_maxX || m_x >= m_xChunk + 16) {
            m_x = Math.max(m_xChunk, m_minX);
            m_z++;
        }

        if (m_z > m_maxZ || m_z >= m_zChunk + 16) {
            m_z = Math.max(m_zChunk, m_minZ);
            m_xChunk += 16;
            m_x = m_xChunk;
        }

        if (m_xChunk > m_maxX) {
            m_x = m_minX;
            m_xChunk = getChunk(m_x);

            m_zChunk += 16;
            m_z = m_zChunk;
        }
    }

    private int getChunk(int i) {
        return (i >> 4) << 4;
    }

    @Override
    public int getCount() {
        return m_count;
    }

    @Override
    public int getAndResetDelta() {
        return m_delta.getAndSet(0);
    }
}
