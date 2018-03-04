/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2017, SBPrime <https://github.com/SBPrime/>
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
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;

/**
 *
 * @author SBPrime
 */
public class ClipboardCommands {
    @Command(
            aliases = {"/copy"},
            flags = "emb",
            desc = "Copy the selection to the clipboard",
            help = "Copy the selection to the clipboard\n"
            + "Flags:\n"
            + "  -e controls whether entities are copied\n"
            + "  -b controls whether biome is copied\n"
            + "  -m sets a source mask so that excluded blocks become air\n"
            + "WARNING: Pasting entities cannot yet be undone!",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.clipboard.copy")
    public void copy(Player player, LocalSession session, EditSession editSession,
            @Selection Region region, @Switch('e') boolean copyEntities,
            @Switch('m') Mask mask, @Switch('b') boolean copyBiomes) throws WorldEditException {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(session.getPlacementPosition(player));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        if (mask != null) {
            copy.setSourceMask(mask);
        }
        copy.setBiomeCopy(copyBiomes);
        
        Operations.completeLegacy(copy);
        session.setClipboard(new ClipboardHolder(clipboard, editSession.getWorld().getWorldData()));

        player.print(region.getArea() + " block(s) were copied.");
    }
    
    @Command(
            aliases = {"/cut"},
            flags = "emb",
            usage = "[leave-id]",
            desc = "Cut the selection to the clipboard",
            help = "Copy the selection to the clipboard\n"
            + "Flags:\n"
            + "  -e controls whether entities are copied\n"
            + "  -b controls whether biome is copied\n"
            + "  -m sets a source mask so that excluded blocks become air\n"
            + "WARNING: Cutting and pasting entities cannot yet be undone!",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.clipboard.cut")
    @Logging(REGION)
    public void cut(Player player, LocalSession session, EditSession editSession,
            @Selection Region region, @Optional("air") Pattern leavePattern, @Switch('e') boolean copyEntities,
            @Switch('m') Mask mask, @Switch('b') boolean copyBiomes) throws WorldEditException {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(session.getPlacementPosition(player));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setSourceFunction(new BlockReplace(editSession, leavePattern));
        if (mask != null) {
            copy.setSourceMask(mask);
        }
        copy.setBiomeCopy(copyBiomes);
        
        Operations.completeLegacy(copy);
        session.setClipboard(new ClipboardHolder(clipboard, editSession.getWorld().getWorldData()));

        player.print(region.getArea() + " block(s) were copied.");
    }
    
    @Command(
            aliases = {"/paste"},
            usage = "",
            flags = "sao",
            desc = "Paste the clipboard's contents",
            help
            = "Pastes the clipboard's contents.\n"
            + "Flags:\n"
            + "  -a skips air blocks\n"
            + "  -b controls whether biome is copied\n"
            + "  -o pastes at the original position\n"
            + "  -s selects the region after pasting",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.clipboard.paste")
    @Logging(PLACEMENT)
    public void paste(Player player, LocalSession session, EditSession editSession,
            @Switch('a') boolean ignoreAirBlocks, @Switch('o') boolean atOrigin,
            @Switch('s') boolean selectPasted, @Switch('b') boolean copyBiomes) throws WorldEditException {

        ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();
        Region region = clipboard.getRegion();

        Vector to = atOrigin ? clipboard.getOrigin() : session.getPlacementPosition(player);
        Operation operation = holder
                .createPaste(editSession, editSession.getWorld().getWorldData())
                .to(to)
                .ignoreAirBlocks(ignoreAirBlocks)
                .build();
        if (operation instanceof ForwardExtentCopy) {
            ((ForwardExtentCopy)operation).setBiomeCopy(copyBiomes);
        }
        
        Operations.completeLegacy(operation);

        if (selectPasted) {
            Vector max = to.add(region.getMaximumPoint().subtract(region.getMinimumPoint()));
            RegionSelector selector = new CuboidRegionSelector(player.getWorld(), to, max);
            session.setRegionSelector(player.getWorld(), selector);
            selector.learnChanges();
            selector.explainRegionAdjust(player, session);
        }

        player.print("The clipboard has been pasted at " + to);
    }
}
