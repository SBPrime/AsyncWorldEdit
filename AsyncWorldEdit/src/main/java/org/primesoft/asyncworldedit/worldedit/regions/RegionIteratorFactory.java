/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.regions.TransformRegion;
import java.util.Collections;
import java.util.Iterator;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.blockPlacer.ICountProvider;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.worldedit.ITaskContext;
import org.primesoft.asyncworldedit.worldedit.TaskContext;

/**
 *
 * @author SBPrime
 */
public final class RegionIteratorFactory {
    
    private RegionIteratorFactory() {}
    
    public static Iterator<BlockVector3> getIterator(Region region) {
        final Iterator<BlockVector3> result = createIterator(region);
        
        if (result instanceof ICountProvider) {
            initializeCountProvider((ICountProvider)result);            
        }
        
        return result;
    }

    private static Iterator<BlockVector3> createIterator(Region region) {
        if ((region == null) || (region instanceof NullRegion)) {
            return Collections.emptyIterator();
        }
        
        if (region instanceof CuboidRegion) {
            return new ChunkCuboidRegionIterator((CuboidRegion)region);
        }
        
        if (region instanceof CylinderRegion ||
                region instanceof Polygonal2DRegion ||
                region instanceof RegionIntersection ||
                region instanceof TransformRegion) {
            //TODO: add in the future
            return null;
        }
        
        if (region instanceof ConvexPolyhedralRegion ||
                region instanceof EllipsoidRegion) {
            return new ChunkBaseRegionIterator((AbstractRegion)region);
        }
        
        return null;
    }
    
    private static void initializeCountProvider(ICountProvider cp) {
        final ITaskContext tc = TaskContext.get();
        final IBlockPlacer bp = tc.getBlockPlacer();
        final IPlayerEntry player = tc.getPlayer();
        
        if (bp == null || player == null) {
            return;
        }
        
        final IBlockPlacerPlayer bpp = bp.getPlayerEvents(player);
        if (bpp == null) {
            return;
        }
        
        bpp.addCounterProvider(cp);
    }
}
