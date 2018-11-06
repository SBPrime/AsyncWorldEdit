/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.Vector2;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;

/**
 *
 * @author SBPrime
 */
public class Vector2Wrapper extends Vector2 implements IAsyncWrapper {
    public static Vector2 wrap(Vector2 v, int jobId, boolean isAsync, IPlayerEntry player) {
        if (v instanceof BlockVector2) {
            return BlockVector2Wrapper.wrap((BlockVector2)v, jobId, isAsync, player);
        }
        
        
        Vector2Wrapper result;
        if (v instanceof Vector2Wrapper) {
            result = (Vector2Wrapper) v;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new Vector2Wrapper(v, jobId, isAsync, player);
        }

        return result;
    }

    private final Vector2 m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private IPlayerEntry m_player;

    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public Vector2 getParent() {
        return m_parent;
    }

    @Override
    public boolean isAsync() {
        return m_isAsync;
    }

    public void setAsync(boolean async) {
        m_isAsync = async;
    }

    public void setPlayer(IPlayerEntry player) {
        m_player = player;
    }

    @Override
    public IPlayerEntry getPlayer() {
        return m_player;
    }

    private Vector2Wrapper(Vector2 parent, int jobId, boolean isAsync, IPlayerEntry player) {
        super(parent.getX(), parent.getZ());

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }

    private Vector2 wrap(Vector2 v) {
        return wrap(v, m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector2 add(Vector2 other) {
        return wrap(m_parent.add(other));
    }

    @Override
    public Vector2 add(Vector2... others) {
        return wrap(m_parent.add(others));
    }

    @Override
    public Vector2 add(double x, double z) {
        return wrap(m_parent.add(x, z));
    }

    @Override
    public Vector2 add(int x, int z) {
        return wrap(m_parent.add(x, z));
    }

    @Override
    public Vector2 ceil() {
        return wrap(m_parent.ceil());
    }

    @Override
    public boolean containedWithin(Vector2 min, Vector2 max) {
        return m_parent.containedWithin(min, max);
    }

    @Override
    public boolean containedWithinBlock(Vector2 min, Vector2 max) {
        return m_parent.containedWithinBlock(min, max);
    }

    @Override
    public double distance(Vector2 pt) {
        return m_parent.distance(pt);
    }

    @Override
    public double distanceSq(Vector2 pt) {
        return m_parent.distanceSq(pt);
    }

    @Override
    public Vector2 divide(Vector2 other) {
        return wrap(m_parent.divide(other));
    }

    @Override
    public Vector2 divide(double n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector2 divide(float n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector2 divide(int n) {
        return wrap(m_parent.divide(n));
    }

    @Override
    public Vector2 divide(double x, double z) {
        return wrap(m_parent.divide(x, z));
    }

    @Override
    public Vector2 divide(int x, int z) {
        return wrap(m_parent.divide(x, z));
    }

    @Override
    public double dot(Vector2 other) {
        return m_parent.dot(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector2Wrapper)
        {
            obj = ((Vector2Wrapper)obj).getParent();
        }
        return m_parent.equals(obj);
    }

    @Override
    public Vector2 floor() {
        return wrap(m_parent.floor());
    }

    @Override
    public int getBlockZ() {
        return m_parent.getBlockZ();
    }

    @Override
    public int getBlockX() {
        return m_parent.getBlockX();
    }

    @Override
    public double getX() {
        return m_parent.getX();
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
    public boolean isCollinearWith(Vector2 other) {
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
    public Vector2 multiply(Vector2 other) {
        return wrap(m_parent.multiply(other));
    }

    @Override
    public Vector2 multiply(Vector2... others) {
        return wrap(m_parent.multiply(others));
    }

    @Override
    public Vector2 multiply(double n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector2 multiply(float n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector2 multiply(int n) {
        return wrap(m_parent.multiply(n));
    }

    @Override
    public Vector2 multiply(double x, double z) {
        return wrap(m_parent.multiply(x, z));
    }

    @Override
    public Vector2 multiply(int x, int z) {
        return wrap(m_parent.multiply(x, z));
    }

    @Override
    public Vector2 normalize() {
        return wrap(m_parent.normalize());
    }

    @Override
    public Vector2 positive() {
        return wrap(m_parent.positive());
    }

    @Override
    public Vector2 round() {
        return wrap(m_parent.round());
    }

    @Override
    public Vector2 setX(double x) {
        return wrap(m_parent.setX(x));
    }

    @Override
    public Vector2 setX(int x) {
        return wrap(m_parent.setX(x));
    }

    @Override
    public Vector2 setZ(double z) {
        return wrap(m_parent.setZ(z));
    }

    @Override
    public Vector2 setZ(int z) {
        return wrap(m_parent.setZ(z));
    }

    @Override
    public Vector2 subtract(Vector2 other) {
        return wrap(m_parent.subtract(other));
    }

    @Override
    public Vector2 subtract(Vector2... others) {
        return wrap(m_parent.subtract(others));
    }

    @Override
    public Vector2 subtract(double x, double z) {
        return wrap(m_parent.subtract(x, z));
    }

    @Override
    public Vector2 subtract(int x, int z) {
        return wrap(m_parent.subtract(x, z));
    }

    @Override
    public BlockVector2 toBlockVector2D() {
        return BlockVector2Wrapper.wrap(m_parent.toBlockVector2D(), m_jobId, m_isAsync, m_player);
    }

    @Override
    public String toString() {
        return m_parent.toString();
    }

    @Override
    public Vector3 toVector() {
        return Vector3Wrapper.wrap(m_parent.toVector(), m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector3 toVector(double y) {
        return Vector3Wrapper.wrap(m_parent.toVector(y), m_jobId, m_isAsync, m_player);
    }

    @Override
    public Vector2 transform2D(double angle, double aboutX, double aboutZ,
                                double translateX, double translateZ) {
        return wrap(m_parent.transform2D(angle, aboutX, aboutZ, translateX, translateZ));
    }
}
