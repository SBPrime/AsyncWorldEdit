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
package org.primesoft.asyncworldedit.worldedit.extent.clipboard;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.LinkedList;
import java.util.List;

/**
 * A thread safe version of BlockArrayClipboard
 *
 * @author SBPrime
 */
public class ThreadSafeBlockArrayClipboard extends BlockArrayClipboard {

    /**
     * The MTA mutex
     */
    private final Object m_mutex = new Object();

    public ThreadSafeBlockArrayClipboard(Region region) {
        super(region);
    }
    
    @Override
    public Region getRegion() {
        final Region result;
        synchronized (m_mutex) {
            result = super.getRegion();
            if (result == null) {
                return null;
            }

            return result.clone();
        }
    }

    @Override
    public Vector getOrigin() {
        synchronized (m_mutex) {
            return super.getOrigin();
        }
    }

    @Override
    public void setOrigin(Vector origin) {
        synchronized (m_mutex) {
            super.setOrigin(origin);
        }
    }

    @Override
    public Vector getDimensions() {
        if (m_mutex == null) {
            return super.getDimensions();
        }
        synchronized (m_mutex) {
            return super.getDimensions();
        }
    }

    @Override
    public Vector getMinimumPoint() {
        synchronized (m_mutex) {
            return super.getMinimumPoint();
        }
    }

    @Override
    public Vector getMaximumPoint() {
        synchronized (m_mutex) {
            return super.getMaximumPoint();
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        final List<? extends Entity> result;
        synchronized (m_mutex) {
            result = super.getEntities(region);

            if (result == null) {
                return null;
            }

            return new LinkedList<Entity>(result);
        }
    }

    @Override
    public List<? extends Entity> getEntities() {
        final List<? extends Entity> result;
        synchronized (m_mutex) {
            result = super.getEntities();

            if (result == null) {
                return null;
            }

            return new LinkedList<Entity>(result);
        }
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        synchronized (m_mutex) {
            return super.createEntity(location, entity);
        }
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        synchronized (m_mutex) {
            return super.getBlock(position);
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        synchronized (m_mutex) {
            return super.getLazyBlock(position);
        }
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        synchronized (m_mutex) {
            return super.setBlock(position, block);
        }
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        synchronized (m_mutex) {
            return super.getBiome(position);
        }
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        synchronized (m_mutex) {
            return super.setBiome(position, biome);
        }
    }

    @Override
    public Operation commit() {
        synchronized (m_mutex) {
            return super.commit();
        }
    }
}
