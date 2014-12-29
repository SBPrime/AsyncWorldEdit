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
package org.primesoft.asyncworldedit.plotme;

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import com.worldcretornica.plotme.PlotManager;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.PlayerEntry;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 * This class is used to fix PlotMe Mask seting errors
 *
 * @author SBPrime
 */
public class PlotMeFix {

    /**
     * Is PlotMe fix enabled
     */
    private boolean m_isEnabled;

    /**
     * The plotMe integrator
     */
    private IPlotMeIntegrator m_integrator;

    /**
     * New instance of PlotMe plugin
     *
     * @param plugin
     */
    public PlotMeFix(JavaPlugin plugin) {
        m_isEnabled = false;
        Plugin cPlugin = null;
        if (ConfigProvider.isPlotMeFixEnabled()) {
            try {
                cPlugin = plugin.getServer().getPluginManager().getPlugin("PlotMe");
                if (cPlugin != null) {
                    m_isEnabled = true;
                }
            } catch (NoClassDefFoundError ex) {
                ExceptionHelper.printException(ex, "Error initializing PlotMe fix.");
            }

            if (m_isEnabled) {
                if (Initialize(cPlugin)) {
                    AsyncWorldEditMain.log("PlotMe fix enabled.");
                } else {
                    AsyncWorldEditMain.log("Error initializing PlotMe fix, unsupported version?.");
                }
            }
        }
    }

    public void setMask(PlayerEntry entry) {
        if (!m_isEnabled || entry == null || entry.getPlayer() == null
                || m_integrator == null) {
            return;
        }

        Player player = entry.getPlayer();
        m_integrator.updateMask(player);
    }

    private boolean Initialize(Plugin instance) {
        String cls = instance.getClass().getCanonicalName();

        IPlotMeIntegrator integrator = null;
        if (cls.equalsIgnoreCase("com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin")) {
            integrator = new PlotMeCoreIntegrator();
        } else if (cls.equalsIgnoreCase("com.worldcretornica.plotme.PlotMe")) {
            integrator = new PlotMeIntegrator();            
        } else {
            return false;
        }

        if (!integrator.initialize(instance)) {
            return false;
        }

        m_integrator = integrator;
        return true;

    }
}
