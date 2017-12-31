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
package org.primesoft.asyncworldedit.utils;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.registry.WorldData;

/**
 *
 * @author SBPrime
 */
public class OrientationTransform {

    private final int[][] m_vectors;
    private final AffineTransform m_transform;
    private final double m_yaw;

    public BlockVector calc(int x, int y, int z) {
        return new BlockVector(m_vectors[0][0] * x + m_vectors[0][1] * y + m_vectors[0][2] * z,
                m_vectors[1][0] * x + m_vectors[1][1] * y + m_vectors[1][2] * z,
                m_vectors[2][0] * x + m_vectors[2][1] * y + m_vectors[2][2] * z);
    }

    public Vector calc(double x, double y, double z) {
        return new Vector(m_vectors[0][0] * x + m_vectors[0][1] * y + m_vectors[0][2] * z,
                m_vectors[1][0] * x + m_vectors[1][1] * y + m_vectors[1][2] * z,
                m_vectors[2][0] * x + m_vectors[2][1] * y + m_vectors[2][2] * z);
    }


    public BaseBlock transform(BaseBlock block, WorldData data) {
        if (data == null || block == null) {
            return block;
        }

        return BlockTransformExtent.transform(block, m_transform, data.getBlockRegistry());
    }

    public double transformRotation(double rot) {
        rot += m_yaw;
        rot -= 360 * (int) (rot / 360);
        if (rot < 0) {
            rot += 360;
        }
        
        return rot;
    }

    public OrientationTransform(double yaw) {
        int heding = 0;

        yaw = (yaw + 360) % 360;

        if (yaw < 45) {
            heding = 0;
        } else if (yaw < 135) {
            heding = 1;
        } else if (yaw < 225) {
            heding = 2;
        } else if (yaw < 315) {
            heding = 3;
        } else {
            heding = 0;
        }

        m_yaw = heding * 90;
        m_transform = new AffineTransform().rotateY(-heding * 90);
        switch (heding) {
            default:
            case 0:
                m_vectors = new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}; //FRONT
                break;
            case 1:
                m_vectors = new int[][]{{0, 0, -1}, {0, 1, 0}, {1, 0, 0}}; //RIGHT
                break;
            case 2:
                m_vectors = new int[][]{{-1, 0, 0}, {0, 1, 0}, {0, 0, -1}}; //BACK
                break;
            case 3:
                m_vectors = new int[][]{{0, 0, 1}, {0, 1, 0}, {-1, 0, 0}}; //LEFT
                break;
        }
    }
}
