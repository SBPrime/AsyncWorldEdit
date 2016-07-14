/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.utils;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 *
 * @author SBPrime
 */
public class PositionHelper {

    /**
     * The size of a chunk un blocks
     */
    public final static int CHUNK_SIZE = 16;

    /**
     * Round the position to block position
     *
     * @param a
     * @return
     */
    public static int positionToBlockPosition(double a) {
        return (int) Math.round(a - 0.5);
    }

    /**
     * Round the position to block position
     *
     * @param v
     * @return
     */
    public static BlockVector positionToBlockPosition(Vector v) {
        return new BlockVector(PositionHelper.positionToBlockPosition(v.getX()), 
                PositionHelper.positionToBlockPosition(v.getY()), 
                PositionHelper.positionToBlockPosition(v.getZ()));
    }

    /**
     * Convert the block position to chunk coords
     *
     * @param a
     * @return
     */
    public static int positionToChunk(double a) {
        final int floor = (int) a;
        final int i = floor == a ? floor : floor - (int) (Double.doubleToRawLongBits(a) >>> 63);
        return i >> 4;
        //return (int) (a / CHUNK_SIZE) + (a % CHUNK_SIZE < 0 ? -1 : 0);
    }


    public static double chunkToPosition(int c) {
        return (double) c * CHUNK_SIZE;
    }

    public static Vector chunkToPosition(Vector2D c, double y) {
        return new Vector(chunkToPosition((int) c.getX()), y, chunkToPosition((int) c.getZ()));
    }

    public static BlockVector2D positionToChunk(Vector v) {
        return new BlockVector2D(positionToChunk((int) v.getX()), positionToChunk((int) v.getZ()));
    }
}
