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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author SBPrime
 */
public class BlockVectorWrapper extends BlockVector implements IAsyncWrapper {
    public static BlockVector wrap(BlockVector v, int jobId,
                                          boolean isAsync, PlayerEntry player) {
        BlockVectorWrapper result;
        if (v instanceof BlockVectorWrapper) {
            result = (BlockVectorWrapper) v;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new BlockVectorWrapper(v, jobId, isAsync, player);
        }

        return result;
    }

    private final BlockVector m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private PlayerEntry m_player;

    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public BlockVector getParent() {
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

    private BlockVectorWrapper(BlockVector parent, int jobId, boolean isAsync,
                               PlayerEntry player) {
        super(0, 0, 0);

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }

    private BlockVector wrap(BlockVector v) {
        return wrap(v, m_jobId, m_isAsync, m_player);
    }

    private Vector wrap(Vector v) {
        if (v instanceof BlockVector) {
            return wrap((BlockVector) v, m_jobId, m_isAsync, m_player);
        }

        return VectorWrapper.wrap(v, m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector add(Vector other) {
        return wrap(m_parent.add(other));
    }

    @Override
    public Vector add(Vector... others) {
        return wrap(m_parent.add(others));
    }

    @Override
    public Vector add(double x, double y, double z) {
        return wrap(m_parent.add(x, y, z));
    }

    @Override
    public Vector add(int x, int y, int z) {
        return wrap(m_parent.add(x, y, z));
    }

    @Override
    public Vector ceil() {
        return wrap(m_parent.ceil());
    }

    @Override
    public Vector clampY(int min, int max) {
        return wrap(m_parent.clampY(min, max));
    }

    @Override
    public int compareTo(Vector other) {
        return m_parent.compareTo(other);
    }

    @Override
    public boolean containedWithin(Vector min, Vector max) {
        return m_parent.containedWithin(min, max);
    }

    @Override
    public boolean containedWithinBlock(Vector min, Vector max) {
        return m_parent.containedWithinBlock(min, max);
    }

    @Override
    public Vector cross(Vector other) {
        return wrap(m_parent.cross(other));
    }

    @Override
    public double distance(Vector pt) {
        return m_parent.distance(pt);
    }

    @Override
    public double distanceSq(Vector pt) {
        return m_parent.distanceSq(pt);
    }

    @Override
    public Vector divide(Vector other) {
        return wrap(m_parent.divide(other));
    }

    @Override
    public Vector divide(double n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector divide(float n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector divide(int n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector divide(double x, double y, double z) {
        return wrap(m_parent.divide(x, y, z));
    }

    @Override
    public Vector divide(int x, int y, int z) {
        return wrap(m_parent.divide(x, y, z));
    }

    @Override
    public double dot(Vector other) {
        return m_parent.dot(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockVectorWrapper) {
            obj = ((BlockVectorWrapper) obj).getParent();
        }
        return m_parent.equals(obj);
    }

    @Override
    public Vector floor() {
        return wrap(m_parent.floor());
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
    public double getX() {
        return m_parent.getX();
    }

    @Override
    public double getY() {
        return m_parent.getY();
    }

    @Override
    public double getZ() {
        return m_parent.getZ();
    }

    @Override
    public int hashCode() {
        return m_parent.hashCode();
    }

    @Override
    public boolean isCollinearWith(Vector other) {
        return m_parent.isCollinearWith(other);
    }

    @Override
    public double length() {
        return m_parent.length();
    }

    @Override
    public double lengthSq() {
        return m_parent.lengthSq();
    }

    @Override
    public Vector multiply(Vector other) {
        return wrap(m_parent.multiply(other));
    }

    @Override
    public Vector multiply(Vector... others) {
        return wrap(m_parent.multiply(others));
    }

    @Override
    public Vector multiply(double n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector multiply(float n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector multiply(int n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector multiply(double x, double y, double z) {
        return wrap(m_parent.multiply(x, y, z));
    }

    @Override
    public Vector multiply(int x, int y, int z) {
        return wrap(m_parent.multiply(x, y, z));
    }

    @Override
    public Vector normalize() {
        return wrap(m_parent.normalize());
    }

    @Override
    public Vector positive() {
        return wrap(m_parent.positive());
    }

    @Override
    public Vector round() {
        return wrap(m_parent.round());
    }

    @Override
    public Vector setX(double x) {
        return wrap(m_parent.setX(x));
    }

    @Override
    public Vector setX(int x) {
        return wrap(m_parent.setX(x));
    }

    @Override
    public Vector setY(double y) {
        return wrap(m_parent.setY(y));
    }

    @Override
    public Vector setY(int y) {
        return wrap(m_parent.setY(y));
    }

    @Override
    public Vector setZ(double z) {
        return wrap(m_parent.setZ(z));
    }

    @Override
    public Vector setZ(int z) {
        return wrap(m_parent.setZ(z));
    }

    @Override
    public Vector subtract(Vector other) {
        return wrap(m_parent.subtract(other));
    }

    @Override
    public Vector subtract(Vector... others) {
        return wrap(m_parent.subtract(others));
    }

    @Override
    public Vector subtract(double x, double y, double z) {
        return wrap(m_parent.subtract(x, y, z));
    }

    @Override
    public Vector subtract(int x, int y, int z) {
        return wrap(m_parent.subtract(x, y, z));
    }

    @Override
    public BlockVector toBlockPoint() {
        return wrap(m_parent.toBlockPoint());
    }

    @Override
    public BlockVector toBlockVector() {
        return wrap(m_parent.toBlockVector());
    }

    @Override
    public String toString() {
        return m_parent.toString();
    }

    @Override
    public Vector2D toVector2D() {
        return Vector2DWrapper.wrap(m_parent.toVector2D(), m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector transform2D(double angle, double aboutX, double aboutZ,
                              double translateX, double translateZ) {
        return wrap(m_parent.transform2D(angle, aboutX, aboutZ, translateX, translateZ));
    }

    @Override
    public float toPitch() {
        return m_parent.toPitch();
    }       

    @Override
    public float toYaw() {
        return m_parent.toYaw();
    }
}
