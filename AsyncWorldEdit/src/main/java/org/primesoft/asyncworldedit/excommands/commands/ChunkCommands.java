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
package org.primesoft.asyncworldedit.excommands.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import org.primesoft.asyncworldedit.api.IAweOperations;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkCommands;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;
import org.primesoft.asyncworldedit.excommands.AsyncCommand;
import org.primesoft.asyncworldedit.worldedit.extent.clipboard.ThreadSafeBlockArrayClipboard;

/**
 * The injected AsyncWorldEdit chunk commands
 *
 * @author SBPrime
 */
public class ChunkCommands {

    /**
     * The AsyncWorldEdit
     */
    private final IAsyncWorldEdit m_asyncWorldEdit;

    /**
     * The DirectChunk commands
     */
    private final IDirectChunkCommands m_dcCommands;

    /**
     * Create a new instance.
     *
     * @param awe the AsyncWorldEdit
     */
    public ChunkCommands(IAsyncWorldEdit awe) {
        m_asyncWorldEdit = awe;
        IAweOperations aweOperations = awe != null ? awe.getOperations() : null;
        m_dcCommands = aweOperations != null ? aweOperations.getChunkOperations() : null;
    }

    /**
     *
     * @param player
     * @param editSession
     * @param region
     * @throws WorldEditException
     */
    @Command(
            aliases = {"clear"},
            usage = "",
            flags = "",
            desc = "Clear the selected schunks.",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.clear")
    public void clear(Player player, EditSession editSession,
            @Selection Region region) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());
        final IAsyncCommand command = m_dcCommands.createClearChunk(playerEntry, region, editSession.getMask());

        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param editSession
     * @param region
     * @throws WorldEditException
     */
    @Command(
            aliases = {"relight"},
            usage = "",
            flags = "v",
            desc = "Relight the selected schunks.",
            help = "Relight the selected schunks.\n"
            + "Flags:\n"
            + "  -v relight using vanilla functions",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.relight")
    public void relight(Player player, EditSession editSession,
            @Selection Region region, @Switch('v') boolean useVanilla) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());
        final IAsyncCommand command = m_dcCommands.createRelight(playerEntry, region, useVanilla);

        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param region
     * @param mask
     * @param copyBiomes
     * @throws WorldEditException
     */
    @Command(
            aliases = {"copy"},
            usage = "",
            flags = "mb",
            desc = "Copy the selection using direct chunk mode.",
            help = "Copy the selection using direct chunk mode.\n"
            + "Flags:\n"
            + "  -b controls whether biome is copied\n"
            + "  -m sets a source mask so that excluded blocks become air",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.copy")
    public void copy(Player player, LocalSession session, EditSession editSession,
            @Selection Region region, @Switch('m') Mask mask, @Switch('b') boolean copyBiomes) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }
        
        final BlockArrayClipboard clipboard = new ThreadSafeBlockArrayClipboard(region);
        clipboard.setOrigin(session.getPlacementPosition(player));

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());        
        final IAsyncCommand command = m_dcCommands.createCopy(playerEntry, region, mask, clipboard, copyBiomes);

        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);

        session.setClipboard(new ClipboardHolder(clipboard, editSession.getWorld().getWorldData()));
    }

    /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param ignoreAirBlocks
     * @param relight
     * @throws WorldEditException
     */
    @Command(
            aliases = {"paste"},
            usage = "",
            flags = "alb",
            desc = "Paste the clipboard using direct chunk mode.",
            help = "Paste the clipboard using direct chunk mode.\n"
            + "Flags:\n"
            + "  -a skips air blocks\n"
            + "  -b controls whether biome is copied\n"
            + "  -l force light recalculation",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.paste")
    public void paste(Player player, LocalSession session, EditSession editSession,
            @Switch('a') boolean ignoreAirBlocks, @Switch('l') boolean relight, @Switch('b') boolean copyBiomes) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }
        
        final ClipboardHolder holder = session.getClipboard();
        final Location l = player.getLocation();
        final World w = player.getWorld();

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createPaste(playerEntry, l, w, 
                editSession.getMask(), holder,
                ignoreAirBlocks, relight, copyBiomes);

        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param editSession
     * @param region
     * @throws WorldEditException
     */
    @Command(
            aliases = {"clone"},
            usage = "",
            flags = "",
            desc = "Clone the selected chunks to current location.",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.clone")
    public void clone(Player player, EditSession editSession,
            @Selection Region region) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }
        
        final Location l = player.getLocation();
        final World w = player.getWorld();

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createClone(playerEntry, region, l, w,
                editSession.getMask());
                
        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param editSession
     * @param region
     * @throws WorldEditException
     */
    @Command(
            aliases = {"fill"},
            usage = "",
            flags = "",
            desc = "Fill the selected chunks with the current chunk.",
            min = 0,
            max = 0
    )
    @CommandPermissions("awe.excommands.chunk.fill")
    public void fill(Player player, EditSession editSession,
            @Selection Region region) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }
        
        final Location l = player.getLocation();
        final World w = player.getWorld();

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createFill(playerEntry, l, w, region, editSession.getMask());                                

        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param editSession
     * @param pattern
     * @param region
     * @param fullChunk
     * @throws WorldEditException
     */
    @Command(
            aliases = {"set"},
            usage = "<block>",
            flags = "f",
            desc = "Set all the blocks inside the selection to a block.",
            help = "Set all the blocks inside the selection to a block.\n"
            + "Flags:\n"
            + "  -f expand selection to whole chunks",
            min = 1,
            max = 1
    )
    @CommandPermissions("awe.excommands.chunk.set")
    public void set(Player player, EditSession editSession,
            Pattern pattern, @Selection Region region,
            @Switch('f') boolean fullChunk) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createSet(playerEntry, region, 
                pattern, editSession.getMask(), fullChunk);
                
        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }
    
    
    /**
     *
     * @param player
     * @param editSession
     * @param pattern
     * @param region
     * @param fullChunk
     * @throws WorldEditException
     */
    @Command(
            aliases = {"setbiome"},
            usage = "<biome>",
            flags = "f",
            desc = "Set the biome inside the selection to provided.",
            help = "Set the biome inside the selection to provided.\n"
            + "Flags:\n"
            + "  -f expand selection to whole chunks",
            min = 1,
            max = 1
    )
    @CommandPermissions("awe.excommands.chunk.setbiome")
    public void setBiome(Player player, EditSession editSession,
            BaseBiome biome, @Selection Region region,
            @Switch('f') boolean fullChunk) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }

        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createSetBiome(playerEntry, region, 
                biome, editSession.getMask(), fullChunk);
                
        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }

    /**
     *
     * @param player
     * @param editSession
     * @param region
     * @param from
     * @param to
     * @param wholeWorld
     * @throws WorldEditException
     */
    @Command(
            aliases = {"replace", "re", "rep"},
            usage = "[from-block] <to-block>",
            flags = "w",
            desc = "Replace all blocks in the selection with another",
            help = "Replace all blocks in the selection with another\n"
            + "Flags:\n"
            + "  -w expand the selection to whole map",
            min = 1,
            max = 2
    )
    @CommandPermissions("awe.excommands.chunk.replace")
    public void replace(Player player, EditSession editSession,
            @Selection Region region,
            @Optional Mask from, Pattern to,
            @Switch('w') boolean wholeWorld) throws WorldEditException {
        if (m_dcCommands == null) {
            return;
        }
        
        final IBlockPlacer blockPlacer = m_asyncWorldEdit.getBlockPlacer();
        final IPlayerEntry playerEntry = m_asyncWorldEdit.getPlayerManager().getPlayer(player.getUniqueId());

        final IAsyncCommand command = m_dcCommands.createReplace(playerEntry, region, from, to, editSession.getMask(), wholeWorld);
        
        AsyncCommand.run(command, playerEntry, blockPlacer, editSession);
    }
}
