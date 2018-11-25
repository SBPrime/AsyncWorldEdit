/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * AsyncWorldEdit Injector a hack plugin that allows AsyncWorldEdit to integrate with
 * the WorldEdit plugin.
 *
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 * Copyright (c) AsyncWorldEdit injector contributors
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

package org.primesoft.asyncworldedit.injector.classfactory;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;

/**
 * Interface for injected WorldEdit classes factory
 * @author SBPrime
 */
public interface IClassFactory {
    /**
     * Get the operation processor
     * @return 
     */
    IOperationProcessor getOperationProcessor();
    
    /**
     * Get the job processor
     * @return 
     */
    IJobProcessor getJobProcessor();
    
    /**
     * Create new instance of the clipboard
     * @param parent
     * @param region
     * @return 
     */
    Clipboard createClipboard(Clipboard parent, Region region);

    /**
     * Add biome copy to region function
     * @param blockCopy
     * @param source
     * @param from
     * @param destination
     * @param to
     * @param currentTransform
     * @param singleSet
     * @return 
     */
    RegionFunction addBiomeCopy(RegionFunction blockCopy, 
            Extent source, BlockVector3 from, Extent destination, BlockVector3 to, 
            Transform currentTransform, boolean singleSet);

    /**
     * Handle the exception from operation
     * @param ex The exception to hanlde 
     * @param name The operation name
     */
    void handleError(WorldEditException ex, String name);

     IPlayerEntry getPlayer(UUID uniqueId);
     
     World wrapWorld(World world, IPlayerEntry player);
}
