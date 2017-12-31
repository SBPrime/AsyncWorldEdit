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
package org.primesoft.asyncworldedit.platform.bukkit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.primesoft.asyncworldedit.LoggerProvider;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.platform.api.IPlayerProvider;
import org.primesoft.asyncworldedit.platform.api.IPlayerStorage;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.versionChecker.VersionCheckResult;
import org.primesoft.asyncworldedit.versionChecker.VersionChecker;

/**
 *
 * @author SBPrime
 */
public class BukkitPlayerProvider implements Listener, IPlayerProvider {

    /**
     * The player storage
     */
    private IPlayerStorage m_playerManager;

    /**
     * The Bukkit server
     */
    private final Server m_server;

    /**
     * The MTA access mutex
     */
    private final Object m_mtaMutex = new Object();

    /**
     * Is the provider initialized
     */
    private boolean isInitialized = false;
    
    /**
     * Instance of the plugin
     */
    private final Plugin m_plugin;

    public BukkitPlayerProvider(Plugin plugin, Server server) {
        m_plugin = plugin;
        m_server = server;
    }

    @Override
    public void initialize(IPlayerStorage playerManager) {
        synchronized (m_mtaMutex) {            
            if (isInitialized) {
                return;
            }
            
            if (playerManager == null) {
                log("Warning: No player storage available!");
                return;
            }
            
            m_playerManager = playerManager;
            isInitialized = true;
        }
        
        for (Player p : m_server.getOnlinePlayers()) {
            m_playerManager.addPlayer(new BukkitPlayerEntry(p));
        }
    }

    @Override
    public void registerEvents() {
        m_server.getPluginManager().registerEvents(this, m_plugin);
    }
    
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        synchronized (m_mtaMutex) {            
            if (!isInitialized) {
                return;
            }                        
        }
        
        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }
                
        m_playerManager.removePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        synchronized (m_mtaMutex) {            
            if (!isInitialized) {
                return;
            }                        
        }
        
        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        final IPlayerEntry entry = m_playerManager.addPlayer(new BukkitPlayerEntry(player));
        
        if (!entry.isAllowed(Permission.ANNOUNCE_VERSION)) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ConfigProvider.getCheckUpdate()) {
                    PluginDescriptionFile desc = m_plugin.getDescription();
                    VersionCheckResult result = VersionChecker.CheckVersion(desc.getVersion());

                    if (!result.getType().equals(VersionCheckResult.Type.Latest)) {
                        entry.say(MessageType.CHECK_VERSION_FORMAT.format(LoggerProvider.PREFIX, result.getMessage()));
                    }
                }
            }
        }).start();
    }     
}
