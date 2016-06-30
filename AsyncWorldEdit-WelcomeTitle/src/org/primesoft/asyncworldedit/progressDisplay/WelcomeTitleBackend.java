/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.progressDisplay;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class WelcomeTitleBackend extends JavaPlugin {
    private static final Logger s_log = Logger.getLogger("Minecraft.AWE.ActionBarAPI");
    private static ConsoleCommandSender s_console;
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";
    
    /**
     * Send message to the log
     *
     * @param msg
     */
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    private static IAsyncWorldEdit getAsyncWorldEdit(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("AsyncWorldEdit");

            if ((cPlugin == null) || (!(cPlugin instanceof AsyncWorldEditBukkit))) {
                AsyncWorldEditBukkit.log("AsyncWorldEdit not found.");
                return null;
            }

            return ((AsyncWorldEditBukkit) cPlugin).getAPI();
        } catch (NoClassDefFoundError ex) {
            ExceptionHelper.printException(ex, "Error initializing AsyncWorldEdit.");
            return null;
        }
    }
    
    /**
     * The AWE API
     */
    private IAsyncWorldEdit m_awe;
    
    /**
     * The progress display manager
     */
    private IProgressDisplayManager m_progressManager;
    
    /**
     * The API integrator
     */
    private IProgressDisplay m_integrator;

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_console = getServer().getConsoleSender();
        
        
        m_awe = getAsyncWorldEdit(this);
        
        if (m_awe == null) {
            log("AsyncWorldEdit API not found.");
            return;
        }
        
        m_progressManager = m_awe.getProgressDisplayManager();
        
        if (m_progressManager == null) {
            log("No progress display manager found.");
            return;
        }
        
        m_integrator = new WelcomeTitleIntegrator();               
        m_progressManager.registerProgressDisplay(m_integrator);
        
        super.onEnable();
        log("Enabled.");
    }

    @Override
    public void onDisable() {
        m_progressManager.unregisterProgressDisplay(m_integrator);
        
        super.onDisable();
    }
}
