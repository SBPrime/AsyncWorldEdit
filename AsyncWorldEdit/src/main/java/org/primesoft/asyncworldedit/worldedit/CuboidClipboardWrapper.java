/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.Region;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.EntityEntry;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 * This class is a wrapper to better handle entity paste Note: Do not use any
 * operations from this class, always use th parrent!
 *
 * @author SBPrime
 */
@Deprecated
public class CuboidClipboardWrapper extends ProxyCuboidClipboard {

    /**
     * The job id
     */
    private final int m_jobId;

    /**
     * The blocks placer
     */
    private final IBlockPlacer m_blocksPlacer;
    /**
     * Player
     */
    private final IPlayerEntry m_player;

    /**
     * Inject entities to CuboidClipboard
     *
     * @param cc
     * @param value
     */
    public static void setEntities(CuboidClipboard cc, Object value) {
        Reflection.set(cc, "entities", value, "Unable to set entities");
    }

    /**
     * Get entities from CuboidClipboard
     *
     * @param cc
     * @return
     */
    public static Object getEntities(CuboidClipboard cc) {
        return Reflection.get(cc, Object.class, "entities", "Unable to get entities");
    }

    public CuboidClipboardWrapper(IPlayerEntry player, CuboidClipboard parrent) {
        this(player, parrent, -1);
    }

    public CuboidClipboardWrapper(IPlayerEntry player, CuboidClipboard parrent, int jobId) {
        super(parrent);

        m_jobId = jobId;
        m_blocksPlacer = AwePlatform.getInstance().getCore().getBlockPlacer();
        m_player = player;
    }

    @Override
    public LocalEntity[] pasteEntities(Vector pos) {
        synchronized (m_parrent) {
            final Object entities = getEntities(m_parrent);
            final int jobId = m_jobId < 0 ? m_blocksPlacer.getJobId(m_player) : m_jobId;
            final EntityEntry entry
                    = new EntityEntry(jobId, entities, pos, m_parrent);

            m_blocksPlacer.addTasks(m_player, entry);
        }
        return new LocalEntity[0];
    }

    @Override
    public void copy(EditSession editSession) {
        CuboidClipboard tmp = new CuboidClipboard(getSize(), getOrigin(), getOffset());
        tmp.copy(editSession);
        
        cloneData(tmp);
    }

    @Override
    public void copy(EditSession editSession, Region region) {
        CuboidClipboard tmp = new CuboidClipboard(getSize(), getOrigin(), getOffset());
        tmp.copy(editSession, region);
        
        cloneData(tmp);
    }

    
    /**
     * Clone data from clipboard to this
     * @param source 
     */
    private void cloneData(CuboidClipboard source) {
        synchronized (m_parrent){
            BaseBlock[][][] data = Reflection.get(source, BaseBlock[][][].class, "data", "Unable to clone clipboard data");
            
            if (data == null) {
                return;
            }
            
            setOffset(source.getOffset());
            setOrigin(source.getOrigin());
            setSize(source.getSize());
            
            Reflection.set(CuboidClipboard.class, m_parrent, "data", data, "Unable to clone clipboard data");
        }
    }
}
