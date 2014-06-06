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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.UUID;

/**
 *
 * @author SBPrime
 */
public class BlockVectorWrapper extends BlockVector implements IAsyncWrapper {
    public static BlockVector wrap(BlockVector v, int jobId,
                                          boolean isAsync, UUID player) {
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

    private UUID m_player;

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

    public void setPlayer(UUID player) {
        m_player = player;
    }

    @Override
    public UUID getPlayer() {
        return m_player;
    }

    private BlockVectorWrapper(BlockVector parent, int jobId, boolean isAsync,
                               UUID player) {
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
}
