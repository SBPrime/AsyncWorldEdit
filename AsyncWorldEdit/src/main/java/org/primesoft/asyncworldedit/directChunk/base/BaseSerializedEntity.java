/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.directChunk.base;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.util.Direction;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;

/**
 *
 * @author SBPrime
 */
public abstract class BaseSerializedEntity implements ISerializedEntity {

    protected final byte[] m_data;
    protected float m_pitch;
    protected Vector m_postion;
    private final UUID m_uuid;
    private ISerializedEntity m_vehicle;
    protected float m_yaw;
    
    
    /**
     * Get the Entity UUID
     *
     * @return
     */
    @Override
    public UUID getUuid() {
        return m_uuid;
    }
        
    /**
     * Get the serialized NBT
     *
     * @return
     */
    @Override
    public byte[] getNBT() {
        return m_data;
    }
    
    /**
     * Get the entity pitch
     *
     * @return
     */
    @Override
    public float getPitch() {
        return m_pitch;
    }
    
    
    /**
     * Set the entity pitch
     *
     * @param angle
     */
    @Override
    public void setPitch(float angle) {
        m_pitch = angle;
    }
    
    
    /**
     * Get the entity Yaw
     *
     * @return
     */
    @Override
    public float getYaw() {
        return m_yaw;
    }
    
    
    /**
     * Set the enity Yaw
     *
     * @param angle
     */
    @Override
    public void setYaw(float angle) {
        m_yaw = angle;
    }
    

    @Override
    public Vector getPosition() {
        return m_postion;
    }
    
    @Override
    public void setPosition(Vector p) {
        m_postion = p;
    }

    

    /**
     * Get the vehicle entity
     *
     * @return
     */
    @Override
    public ISerializedEntity getVehicle() {
        return m_vehicle;
    }

    /**
     * Get the vehicle entity
     *
     * @param vehicle
     */
    @Override
    public void setVehicle(ISerializedEntity vehicle) {
        m_vehicle = vehicle;
    }
    

    protected BaseSerializedEntity(UUID uuid, Vector position, float yaw, float pitch, byte[] data) {
        if (!BaseChunkData.isValidPosition(position)) {
            throw new IllegalArgumentException(String.format("Provided position is out of range: %1$s", position));
        }

        m_postion = position;
        m_yaw = yaw;
        m_pitch = pitch;

        m_uuid = uuid;
        m_data = data;
    }

    /**
     * Get the Direction for yaw
     *
     * @param yaw
     * @return
     */
    protected static Direction findDirection(float yaw) {
        switch ((int) yaw) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.WEST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
            default:
                return Direction.SOUTH;
        }
    }
}