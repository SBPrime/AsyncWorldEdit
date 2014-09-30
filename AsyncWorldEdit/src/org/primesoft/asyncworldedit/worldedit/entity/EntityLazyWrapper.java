/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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
package org.primesoft.asyncworldedit.worldedit.entity;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;

/**
 *
 * @author SBPrime
 */
public class EntityLazyWrapper implements Entity {

    /**
     * The wrapped entity
     */
    private Entity m_entity;

    /**
     * Is the entity removed
     */
    private boolean m_isRemoved;

    private Location m_defaultLocation;

    private BaseEntity m_defaultState;

    private Extent m_defaultExtent;

    public EntityLazyWrapper(Location location, Extent extent) {
        m_isRemoved = false;
        m_defaultExtent = extent;
        m_defaultLocation = location;
        m_defaultState = null;
        m_entity = null;
    }

    @Override
    public BaseEntity getState() {
        Entity entity = m_entity;
        m_defaultState = entity != null ? entity.getState() : m_defaultState;
        return m_defaultState;
    }

    @Override
    public Location getLocation() {
        Entity entity = m_entity;
        m_defaultLocation = entity != null ? entity.getLocation() : m_defaultLocation;
        return m_defaultLocation;
    }

    @Override
    public Extent getExtent() {
        Entity entity = m_entity;
        m_defaultExtent = entity != null ? entity.getExtent() : m_defaultExtent;
        return m_defaultExtent;
    }

    @Override
    public boolean remove() {
        Entity entity = m_entity;

        if (entity == null) {
            m_isRemoved = true;
            return true;
        }

        m_isRemoved = entity.remove();
        return m_isRemoved;
    }

    @Override
    public <T> T getFacet(Class<? extends T> type) {
        Entity entity = m_entity;

        return entity != null ? entity.getFacet(type) : null;
    }

    
    /**
     * Sets the wrapped entity
     * @param entity 
     */
    public void setEntity(Entity entity) {
        if (m_isRemoved)
        {
            entity.remove();
            return;
        }
        
        m_entity = entity;
    }
}
