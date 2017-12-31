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
package org.primesoft.asyncworldedit.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author SBPrime
 */
public class MultiThreadExtent implements Extent {
    /**
     * The default extent
     */
    private Extent m_default;
    
    /**
     * Thread delegate extents
     */
    private final HashMap<Thread, Extent> m_extents = new LinkedHashMap<Thread, Extent>();
    
    public MultiThreadExtent() { }
    
    /**
     * Set the default extent
     * @param e 
     */
    public void setDefault(Extent e) {
        m_default = e;
    }
    
    /**
     * Set extent for thread
     * @param t
     * @param e 
     */
    public void setExtent(Thread t, Extent e) {
        synchronized (m_extents) {
            if (e == null) {
                m_extents.remove(t);
            } else {
                m_extents.put(t, e);
            }
        }
    }
    
    /**
     * Set extent for current thread
     * @param e 
     */
    public void setExtent(Extent e) {
        setExtent(Thread.currentThread(), e);
    }
    
    
    /**
     * Get extent for current thread
     * @return 
     */
    private Extent getExtent() {
        final Thread current = Thread.currentThread();
        synchronized (m_extents) {
            if (m_extents.containsKey(current)) {
                return m_extents.get(current);
            }
        }
        
        return m_default;
    }

    @Override
    public Vector getMinimumPoint() {
        return getExtent().getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return getExtent().getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return getExtent().getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return getExtent().getEntities();
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return getExtent().createEntity(location, entity);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return getExtent().getBlock(position);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return getExtent().getLazyBlock(position);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return getExtent().getBiome(position);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        return getExtent().setBlock(position, block);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return getExtent().setBiome(position, biome);
    }

    @Override
    public Operation commit() {
        return getExtent().commit();
    }
    
}
