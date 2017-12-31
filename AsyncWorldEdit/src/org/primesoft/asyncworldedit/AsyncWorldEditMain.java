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
package org.primesoft.asyncworldedit;

import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

import org.primesoft.asyncworldedit.api.IPlotMeFix;
import org.primesoft.asyncworldedit.api.IAweOperations;
import org.primesoft.asyncworldedit.api.IAdapter;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.changesetSerializer.ISerializerManager;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.IWorldeditIntegrator;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.classScanner.IClassScannerOptions;

/**
 * The plugin main class
 *
 * @author SBPrime
 */
public abstract class AsyncWorldEditMain extends JavaPlugin implements IAsyncWorldEdit {

    private static AsyncWorldEditMain s_instance;

    protected IAsyncWorldEditCore m_api;

    @Override
    public double getAPIVersion() {
        return m_api == null ? -1 : m_api.getAPIVersion();
    }

    @Override
    public final byte[] getDetailAPIVersion() {
        return m_api == null ? null : m_api.getDetailAPIVersion();
    }

    /**
     * Get teh native API adapter
     *
     * @return
     */
    @Override
    public IAdapter getAdapter() {
        return m_api == null ? null : m_api.getAdapter();
    }

    /**
     * Get the direct chunk API
     *
     * @return
     */
    @Override
    public IDirectChunkAPI getDirectChunkAPI() {
        return m_api == null ? null : m_api.getDirectChunkAPI();
    }

    @Override
    public IAweOperations getOperations() {
        return m_api == null ? null : m_api.getOperations();
    }

    /**
     * Get the map manipulation utils
     *
     * @return
     */
    @Override
    public IMapUtils getMapUtils() {
        return m_api == null ? null : m_api.getMapUtils();
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return m_api == null ? null : m_api.getPlayerManager();
    }

    @Override
    public IPhysicsWatch getPhysicsWatcher() {
        return m_api == null ? null : m_api.getPhysicsWatcher();
    }

    @Override
    public IBlockPlacer getBlockPlacer() {
        return m_api == null ? null : m_api.getBlockPlacer();
    }

    @Override
    public ITaskDispatcher getTaskDispatcher() {
        return m_api == null ? null : m_api.getTaskDispatcher();
    }

    @Override
    public IProgressDisplayManager getProgressDisplayManager() {
        return m_api == null ? null : m_api.getProgressDisplayManager();
    }

    @Override
    public ISerializerManager getChangesetSerializer() {
        return m_api == null ? null : m_api.getChangesetSerializer();
    }

    @Override
    public IWorldeditIntegrator getWorldEditIntegrator() {
        return (m_api == null) ? null : m_api.getWorldEditIntegrator();
    }

    @Override
    public IWorld getWorld(UUID worldUUID) {
        return m_api == null ? null : m_api.getWorld(worldUUID);
    }

    @Override
    public IWorld getWorld(String worldName) {
        return m_api == null ? null : m_api.getWorld(worldName);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        
        s_instance = this;
    }       

    @Override
    public IClassScannerOptions getClassScannerOptions() {
        return m_api == null ? null : m_api.getClassScannerOptions();
    }
    
    

    public static AsyncWorldEditMain getInstance() {
        return s_instance;
    }

    public IAsyncWorldEdit getAPI() {
        return m_api;
    }

    public IPlotMeFix getPlotMeFix() {
        return m_api == null ? null : m_api.getPlotMeFix();
    }

    public void setPlotMeFix(IPlotMeFix plotMeFix) {
        if (m_api == null) {
            return;
        }
        m_api.setPlotMeFix(plotMeFix);
    }
}
