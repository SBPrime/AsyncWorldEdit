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

package org.primesoft.asyncworldedit.worldedit.util;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;
import org.primesoft.asyncworldedit.worldedit.VectorWrapper;


/**
 *
 * @author SBPrime
 */
public class LocationWrapper extends Location implements IAsyncWrapper
{
    public static LocationWrapper wrap(Location location, int jobId,
                                        boolean isAsync, UUID player) {
        LocationWrapper result;
        if (location instanceof LocationWrapper) {
            result = (LocationWrapper) location;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new LocationWrapper(location, jobId, isAsync, player);
        }

        return result;
    }
    
    private final Location m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private UUID m_player;
    
    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public Location getParent() {
        return m_parent;
    }

    @Override
    public boolean isAsync() {
        return m_isAsync;
    }
    
    public void setAsync(boolean async) {
        m_isAsync = async;
    }

    public void setPlayer(UUID player) {
        m_player = player;
    }

    @Override
    public UUID getPlayer() {
        return m_player;
    }
    
    private LocationWrapper(Location parent, int jobId, boolean isAsync,
                             UUID player) {
        super(parent.getExtent());

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof LocationWrapper) {
            o = ((LocationWrapper) o).getParent();
        }
        return m_parent.equals(o);
    }

    @Override
    public int getBlockX() {
        return m_parent.getBlockX();
    }

    @Override
    public int getBlockY() {
        return m_parent.getBlockY();
    }

    @Override
    public int getBlockZ() {
        return m_parent.getBlockZ();
    }

    @Override
    public Vector getDirection() {
        return VectorWrapper.wrap(m_parent.getDirection(), m_jobId, m_isAsync, m_player);
    }

    @Override
    public Extent getExtent() {
        return m_parent.getExtent();
    }

    @Override
    public float getPitch() {
        return m_parent.getPitch();
    }

    @Override
    public double getX() {
        return m_parent.getX();
    }

    @Override
    public double getY() {
        return m_parent.getY();
    }

    @Override
    public float getYaw() {
        return m_parent.getYaw();
    }

    @Override
    public double getZ() {
        return m_parent.getZ();
    }

    @Override
    public Location setDirection(Vector direction) {
        return LocationWrapper.wrap(m_parent.setDirection(direction), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setDirection(float yaw, float pitch) {
        return LocationWrapper.wrap(m_parent.setDirection(yaw, pitch),
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setExtent(Extent extent) {
        return LocationWrapper.wrap(m_parent.setExtent(extent),
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setPitch(float pitch) {
        return LocationWrapper.wrap(m_parent.setPitch(pitch), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setX(double x) {
        return LocationWrapper.wrap(m_parent.setX(x),
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setX(int x) {
        return LocationWrapper.wrap(m_parent.setX(x),
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setY(double y) {
        return LocationWrapper.wrap(m_parent.setY(y), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setY(int y) {
        return LocationWrapper.wrap(m_parent.setY(y), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setYaw(float yaw) {
        return LocationWrapper.wrap(m_parent.setYaw(yaw), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setZ(double z) {
        return LocationWrapper.wrap(m_parent.setZ(z), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Location setZ(int z) {
        return LocationWrapper.wrap(m_parent.setZ(z), 
                m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector toVector() {
        return VectorWrapper.wrap(m_parent.toVector(),
                m_jobId, m_isAsync, m_player);
    }          
}
