/*
 * AsyncWorldEdit API
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
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
package org.primesoft.asyncworldedit.api.directChunk;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;

/**
 *
 * @author SBPrime
 */
public interface IDirectChunkCommands {
    /**
     * Create the clear chunk command
     * @param playerEntry The player entry
     * @param region Region to clear
     * @param mask Mask
     * @return 
     */
    IAsyncCommand createClearChunk(IPlayerEntry playerEntry, Region region, Mask mask);

    /**
     * Create the relight chunk command
     * @param playerEntry The player entry
     * @param region Region to clear
     * @return 
     */
    IAsyncCommand createRelight(IPlayerEntry playerEntry, Region region);

    /**
     * Create the copy to clipboard command
     * @param playerEntry
     * @param region
     * @param mask
     * @param clipboard
     * @return 
     */
    IAsyncCommand createCopy(IPlayerEntry playerEntry, Region region, Mask mask, Clipboard clipboard);

    /**
     * Create the clipboard paste command
     * @param playerEntry
     * @param position
     * @param world
     * @param mask
     * @param clipboard
     * @param ignoreAirBlocks
     * @param relight
     * @return 
     */
    IAsyncCommand createPaste(IPlayerEntry playerEntry, Location position, World world, Mask mask, ClipboardHolder clipboard, 
            boolean ignoreAirBlocks, boolean relight);

    /**
     * Create clone chunk command
     * @param playerEntry
     * @param region Source region
     * @param position Target position
     * @param world
     * @param mask
     * @return 
     */
    IAsyncCommand createClone(IPlayerEntry playerEntry, Region region, Location position, World world, Mask mask);

    
    /**
     * Create fill chunk command
     * @param playerEntry
     * @param position Source position
     * @param world Source world
     * @param region Target region (region to fill)
     * @param mask
     * @return 
     */
    IAsyncCommand createFill(IPlayerEntry playerEntry, Location position, World world, Region region, Mask mask);

    /**
     * Create set chunk command
     * @param playerEntry
     * @param region
     * @param pattern
     * @param mask
     * @param fullChunk
     * @return 
     */
    IAsyncCommand createSet(IPlayerEntry playerEntry, Region region, Pattern pattern, Mask mask, boolean fullChunk);

    /**
     * Create replace chunk command
     * @param playerEntry
     * @param region
     * @param from
     * @param to
     * @param mask
     * @param wholeWorld perform the replace over the whole world
     * @return 
     */
    IAsyncCommand createReplace(IPlayerEntry playerEntry, Region region, Mask from, Pattern to, Mask mask, boolean wholeWorld);
}
