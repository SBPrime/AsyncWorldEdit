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
package org.primesoft.asyncworldedit.excommands;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BoundedHeightMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import org.primesoft.asyncworldedit.worldedit.function.mask.DeltaMask;
import org.primesoft.asyncworldedit.worldedit.function.visitor.ConfigurableVisitor;

/**
 *
 * @author SBPrime
 */
public class FillCommand extends AsyncCommand {

    private final int FLAG_X = 0x1;
    private final int FLAG_Y = 0x2;
    private final int FLAG_Z = 0x4;
    private final int FLAG_XY = FLAG_X | FLAG_Y;
    private final int FLAG_XZ = FLAG_X | FLAG_Z;
    private final int FLAG_YZ = FLAG_Y | FLAG_Z;
    private final int FLAG_XYZ = FLAG_X | FLAG_Y | FLAG_Z;

    /**
     * The operation name
     */
    private final WorldeditOperations m_operation;
    private final Vector m_pos;
    private final Pattern m_pattern;
    private final int m_depth;
    private final double m_radius;
    private final boolean m_axisX;
    private final boolean m_axisY;
    private final boolean m_axisZ;

    @Override
    public String getName() {
        return m_operation.toString();
    }

    public FillCommand(IPlayerEntry player, Vector pos,
            Pattern pattern, double radius, int depth,
            boolean axisX, boolean axisY, boolean axisZ) {
        super(player);

        m_pos = pos;
        m_pattern = pattern;
        m_radius = radius;
        m_depth = depth;
        m_axisX = axisX;
        m_axisY = axisY;
        m_axisZ = axisZ;
        
        
        int id = (axisX ? FLAG_X : 0x00)
                | (axisY ? FLAG_Y : 0x00)
                | (axisZ ? FLAG_Z : 0x00);

        switch (id) {
            case FLAG_XY:
                m_operation = WorldeditOperations.fillXY;
                break;
            case FLAG_XZ:
                m_operation = WorldeditOperations.fillXZ;
                break;
            case FLAG_YZ:
                m_operation = WorldeditOperations.fillZY;
                break;
            case FLAG_XYZ:
                m_operation = WorldeditOperations.fill3d;
                break;
            default:
                m_operation = null;
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws WorldEditException {
        MaskIntersection mask = new MaskIntersection(
                new RegionMask(new EllipsoidRegion(m_pos, new Vector(m_radius, m_radius, m_radius))),
                new DeltaMask(m_pos, m_depth, !m_axisX, !m_axisY, !m_axisZ),
                Masks.negate(new ExistingBlockMask(editSesstion)));

        // Want to replace blocks
        BlockReplace replace = new BlockReplace(editSesstion, m_pattern);

        // Pick how we're going to visit blocks
        RecursiveVisitor visitor = new ConfigurableVisitor(mask, replace, 
                m_axisX, m_axisY, m_axisZ);     
        

        // Start at the origin
        visitor.visit(m_pos);

        // Execute
        Operations.completeLegacy(visitor);

        return visitor.getAffected();
    }
}
