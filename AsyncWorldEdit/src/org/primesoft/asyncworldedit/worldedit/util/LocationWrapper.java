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

package org.primesoft.asyncworldedit.worldedit.util;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;
import org.primesoft.asyncworldedit.worldedit.VectorWrapper;


/**
 *
 * @author SBPrime
 */
public class LocationWrapper extends Location implements IAsyncWrapper
{
    public static LocationWrapper wrap(Location location, int jobId,
                                        boolean isAsync, PlayerEntry player) {
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

    private PlayerEntry m_player;
    
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

    public void setPlayer(PlayerEntry player) {
        m_player = player;
    }

    @Override
    public PlayerEntry getPlayer() {
        return m_player;
    }
    
    private LocationWrapper(Location parent, int jobId, boolean isAsync,
                             PlayerEntry player) {
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
