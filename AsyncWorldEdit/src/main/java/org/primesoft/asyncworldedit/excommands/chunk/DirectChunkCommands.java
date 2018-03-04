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
package org.primesoft.asyncworldedit.excommands.chunk;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkCommands;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;

/**
 *
 * @author SBPrime
 */
public class DirectChunkCommands implements IDirectChunkCommands {

    private final IAsyncWorldEditCore m_awe;

    public DirectChunkCommands(IAsyncWorldEditCore awe) {
        m_awe = awe;
    }

    @Override
    public IAsyncCommand createClearChunk(IPlayerEntry playerEntry, Region region, Mask mask) {
        return new ClearChunkCommand(region, m_awe, mask, playerEntry);
    }

    @Override
    public IAsyncCommand createRelight(IPlayerEntry playerEntry, Region region, boolean vanilla) {
        return new RelightChunkCommand(region, m_awe, playerEntry, vanilla);
    }
    
    @Override
    public IAsyncCommand createRelight(IPlayerEntry playerEntry, Region region) {
        return createRelight(playerEntry, region, true);
    }

    @Override
    public IAsyncCommand createCopy(IPlayerEntry playerEntry, Region region, Mask mask, Clipboard clipboard) {
        return createCopy(playerEntry, region, mask, clipboard, false);
    }
    
    @Override
    public IAsyncCommand createPaste(IPlayerEntry playerEntry, Location position, World world, Mask mask, ClipboardHolder clipboard, boolean ignoreAirBlocks, boolean relight) {
        return createPaste(playerEntry, position, world, mask, clipboard, ignoreAirBlocks, relight, false);
    }

    @Override
    public IAsyncCommand createPaste(IPlayerEntry playerEntry, Location position, World world, Mask mask, ClipboardHolder clipboard, 
            boolean ignoreAirBlocks, boolean relight, boolean copyBiome) {
        return new PasteChunkCommand(clipboard, position, world,
                m_awe, ignoreAirBlocks, relight, copyBiome, world.getWorldData(), mask,
                playerEntry);
    }
    
    @Override
    public IAsyncCommand createCopy(IPlayerEntry playerEntry, Region region, Mask mask, Clipboard clipboard,
            boolean  copyBiome) {
        return new CopyChunkCommand(clipboard, region, m_awe, copyBiome, mask, playerEntry);
    }
    

    @Override
    public IAsyncCommand createClone(IPlayerEntry playerEntry, Region region, Location position, World world, Mask mask) {
        return new CloneChunkCommand(region, position, world,
                m_awe, mask, playerEntry);

    }

    @Override
    public IAsyncCommand createFill(IPlayerEntry playerEntry, Location position, World world, Region region, Mask mask) {
        return new FillChunkCommand(region, position, world,
                m_awe, mask, playerEntry);
    }

    @Override
    public IAsyncCommand createSet(IPlayerEntry playerEntry, Region region, Pattern pattern, Mask mask, boolean fullChunk) {
        return new SetChunkCommand(region, fullChunk,
                pattern, m_awe, mask, playerEntry);
    }
    
    @Override
    public IAsyncCommand createSetBiome(IPlayerEntry playerEntry, Region region, BaseBiome biome, Mask mask, boolean fullChunk) {
        return new SetBiomeChunkCommand(region, fullChunk,
                biome, m_awe, mask, playerEntry);
    }

    @Override
    public IAsyncCommand createReplace(IPlayerEntry playerEntry, Region region, Mask from, Pattern to, Mask mask, boolean wholeWorld) {
        return new ReplaceChunkCommand(region, wholeWorld,
                from, to, m_awe, mask, playerEntry);
    }
}
