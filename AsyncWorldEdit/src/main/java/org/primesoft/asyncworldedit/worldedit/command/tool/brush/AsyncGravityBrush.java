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
package org.primesoft.asyncworldedit.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.command.tool.brush.GravityBrush;
import com.sk89q.worldedit.function.pattern.Pattern;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 * This code is based mostly on GravityBrush from WorldEdit.
 *
 * @author SBPrime
 */
public class AsyncGravityBrush extends GravityBrush {

    private static final Field s_gravityBrushFullHeight
            = Reflection.findField(GravityBrush.class, "fullHeight", "Unable to get fullHeight field from GravityBrush");

    public static Brush wrap(GravityBrush brush) {
        if (brush == null || s_gravityBrushFullHeight == null) {
            return brush;
        }

        Boolean fullHeight = Reflection.get(brush, Boolean.class, s_gravityBrushFullHeight, "Unable to get fullHeight from GravityBrush");

        return new AsyncGravityBrush(fullHeight == null ? false : fullHeight);
    }

    private final boolean m_fullHeight;

    private AsyncGravityBrush(boolean fullHeight) {
        super(fullHeight);

        m_fullHeight = fullHeight;
    }

    @Override
    public void build(EditSession editSession, Vector position, Pattern pattern, double size) throws MaxChangedBlocksException {
        final BaseBlock air = new BaseBlock(BlockID.AIR, 0);
        final double startY = m_fullHeight ? editSession.getWorld().getMaxY() : position.getBlockY() + size;
        final double endY = position.getBlockY() - size;
        
        for (double x = position.getBlockX() + size; x > position.getBlockX() - size; --x) {
            for (double z = position.getBlockZ() + size; z > position.getBlockZ() - size; --z) {
                final List<BaseBlock> blockTypes = new ArrayList<BaseBlock>();
                double y = startY;
                for (; y > endY; --y) {
                    final Vector pt = new Vector(x, y, z);
                    final BaseBlock block = editSession.getBlock(pt);
                    if (!block.isAir()) {
                        blockTypes.add(block);
                    }
                }
                
                Vector pt = new Vector(x, y, z);
                Collections.reverse(blockTypes);
                
                
                y = startY;
                for (BaseBlock block : blockTypes) {
                    editSession.setBlock(pt, block);
                    pt = pt.add(0, 1, 0);
                    --y;
                }
                
                while (y >= endY) {
                    editSession.setBlock(pt, air);
                    pt = pt.add(0, 1, 0);
                    --y;
                }
            }
        }
    }

}
