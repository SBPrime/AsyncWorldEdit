/*
 * The MIT License
 *
 * Copyright 2012 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author SBPrime
 */
public class EventListener implements Listener {

    private PluginMain m_parent;

    public EventListener(PluginMain parent) {
        m_parent = parent;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        m_parent.getPlayerManager().removePlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        m_parent.getPlayerManager().addPlayer(player);

        if (!PermissionManager.isAllowed(player, PermissionManager.Perms.AnnounceVersion)) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (ConfigProvider.getCheckUpdate()) {
                    PluginDescriptionFile desc = m_parent.getDescription();
                    PluginMain.say(player, ChatColor.BLUE + PluginMain.getPrefix()
                            + VersionChecker.CheckVersion(desc.getVersion()));
                }
            }
        }).start();

        if (!ConfigProvider.isConfigUpdated()) {
            PluginMain.say(player, ChatColor.BLUE + PluginMain.getPrefix()
                    + "Please update your config file!");
        }
    }    
}
