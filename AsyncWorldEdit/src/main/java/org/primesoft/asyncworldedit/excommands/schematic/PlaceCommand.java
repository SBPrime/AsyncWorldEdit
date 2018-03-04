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
package org.primesoft.asyncworldedit.excommands.schematic;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.AweEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.io.Closer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.excommands.AsyncCommand;
import org.primesoft.asyncworldedit.schematics.ISchematicReader;
import org.primesoft.asyncworldedit.schematics.SchematicData;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.OrientationTransform;

/**
 *
 * @author SBPrime
 */
public class PlaceCommand extends AsyncCommand {

    private final Player m_player;
    private final ISchematicReader m_schematicReader;
    private final String m_fileName;
    private final File m_file;
    private final OrientationTransform m_orientation;
    private final BlockVector m_to;
    private final boolean m_ignoreAirBlocks;

    public PlaceCommand(Player player, IPlayerEntry playerEntry, String filename, File file, ISchematicReader schematicReader,
            BlockVector to, OrientationTransform orientation, boolean ignoreAirBlocks) {
        super(playerEntry);
        
        m_player = player;
        m_fileName = filename;
        m_schematicReader = schematicReader;
        m_file = file;
        m_to = to;
        m_orientation = orientation;
        m_ignoreAirBlocks = ignoreAirBlocks;
    }

    @Override
    public String getName() {
        return "placeSchematic";
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws MaxChangedBlocksException {
        Closer closer = Closer.create();
        SchematicData info;
        try {
            FileInputStream fis = closer.register(new FileInputStream(m_file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            
            info = m_schematicReader.readData(bis);
        } catch (IOException ex) {
            m_player.printError(MessageType.EX_CMD_SCHEMATIC_READ_ERROR.format(ex.getMessage()));
            ExceptionHelper.printException(ex, String.format("Unable to read schematic %1$s", m_fileName));
            return 0;
        } finally {
            try {
                closer.close();
            } catch (IOException ex) {
                //Ignore error
            }
        }
        
        if (info == null) {
            m_player.printError(MessageType.EX_CMD_SCHEMATIC_READ_ERROR.format("NullPTR"));
            return 0;
        }
        
        if (!info.isValid()) {
            m_player.printError(MessageType.EX_CMD_SCHEMATIC_INVALID_FORMAT.format());
            return 0;
        }
        
        m_player.print(MessageType.EX_CMD_SCHEMATIC_LOADED.format());
        return m_schematicReader.place(m_player.getWorld().getWorldData(), m_to, m_orientation, 
                m_ignoreAirBlocks,
                info, editSesstion);
    }
}
