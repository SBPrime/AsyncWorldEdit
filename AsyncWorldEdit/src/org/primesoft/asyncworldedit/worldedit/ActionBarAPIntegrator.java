/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
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
package org.primesoft.asyncworldedit.worldedit;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class ActionBarAPIntegrator
{

    private final boolean m_isInitialized;
    private static final String light = "░";
    private static final String dark = "█";
    private static int barAmount = 20;

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    public static ActionBarAPI getABAPI(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("ActionBarAPI");

            if ((cPlugin == null) || (!(cPlugin instanceof ActionBarAPI))) {
                AsyncWorldEditMain.log("ActionBarAPI not found.");
                return null;
            }

            return (ActionBarAPI) cPlugin;
        } catch (NoClassDefFoundError ex) {
            ExceptionHelper.printException(ex, "Error initializing BarAPI.");
            return null;
        }
    }

    public ActionBarAPIntegrator(JavaPlugin plugin) {
        ActionBarAPI ba = getABAPI(plugin);
        m_isInitialized = ba != null;
    }

    public void setMessage(PlayerEntry player, String message, double percent) {
        if (!m_isInitialized || player == null || player.getPlayer() == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }

        if (message == null) {
            message = "";
        }
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        int increment = 100/barAmount;
        int darkAmount = (int) percent/increment;
        int lightAmount = barAmount-darkAmount;

        String bars = "";
        for(int i = 0; i < darkAmount; i++)
            bars+=dark;
        for(int i = 0; i < lightAmount; i++)
            bars+=light;

        message += " : "+bars+" "+(int) percent+"%";
        ActionBarAPI.sendActionBar(player.getPlayer(), message);
    }

    public void disableMessage(PlayerEntry player) {
        if (!m_isInitialized || player == null || player.getPlayer() == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }
        ActionBarAPI.sendActionBar(player.getPlayer(), "");
    }
}