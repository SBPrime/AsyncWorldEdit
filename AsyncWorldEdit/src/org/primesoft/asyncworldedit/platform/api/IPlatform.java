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
package org.primesoft.asyncworldedit.platform.api;

import java.util.UUID;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.strings.MessageProvider;

/**
 *
 * @author SBPrime
 */
public interface IPlatform {
    /**
     * Get the platform name
     * @return 
     */
    String getName();

    /**
     * Get the plugin version
     * @return 
     */
    String getVersion();
    
    
    /**
     * Gets the server class
     * @return 
     */
    String getServerAPI();
    
    /**
     * Gets the server api version
     * @return 
     */
    String getServerAPILong();
    
    /**
     * Get the command manager
     * @return 
     */
    ICommandManager getCommandManager();
    
    /**
     * Get the player provider
     * @return 
     */
    IPlayerProvider getPlayerProvider();


    /**
     * Get the scheduler
     * @return 
     */
    IScheduler getScheduler();
    
    /**
     * Initialize the platform
     * @param core 
     */
    void initialize(IAsyncWorldEditCore core);

    void onEnable();
    
    void onDisable();

    
    /**
     * Get the plugin configuration
     * @return 
     */
    IConfiguration getConfig();

    
    /**
     * The physics watcher
     * @return 
     */
    IPhysicsWatch getPhysicsWatcher();
    
    /**
     * Get the chunk watcher
     * @return 
     */
    IChunkWatch getChunkWatcher();
    
    
    /**
     * Get the AsyncWorldEdit world
     * @param worldUUID
     * @return 
     */
    IWorld getWorld(UUID worldUUID);
    
    /**
     * Get the AsyncWorldEdit world
     * @param worldName
     * @return 
     */
    IWorld getWorld(String worldName);
    
    
    /**
     * Get the map utils
     * @return 
     */
    IMapUtils getMapUtils();

    /**
     * Get the material library
     * @return 
     */
    IMaterialLibrary getMaterialLibrary();

    
    /**
     * Create the WorldEdit integrator
     * @return 
     */
    IWorldeditIntegratorInner getWorldEditIntegrator();
    
    
    /**
     * Create the message provider
     * @return 
     */
    MessageProvider createMessageProvider();

    /**
     * The class scanner
     * @return 
     */
    ClassScanner getClasScanner();

    
    /**
     * Get plugin
     * @param pluginName
     * @return 
     */
    Object getPlugin(String pluginName);

    /**
     * Reload the plugin config
     */
    void reloadConfig();
}
