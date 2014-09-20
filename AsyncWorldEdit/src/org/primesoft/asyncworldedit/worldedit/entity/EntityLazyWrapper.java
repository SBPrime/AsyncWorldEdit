/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
