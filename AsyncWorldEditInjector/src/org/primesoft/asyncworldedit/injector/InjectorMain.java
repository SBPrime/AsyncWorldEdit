/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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
package org.primesoft.asyncworldedit.injector;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.operation.Operations;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class InjectorMain extends JavaPlugin {
    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";
    
    /**
     * The WorldEdit class factory
     */
    private IClassFactory m_classFactory = new BaseClassFactory();
    
    /**
     * Instance of the main class
     */
    private static InjectorMain s_instance;

    /**
     * Get plugin instance
     * @return 
     */
    public static InjectorMain getInstance() {
        return s_instance;
    }
       
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }
    
    
    /**
     * Set new class factory
     * @param factory 
     */    
    public void setClassFactory(IClassFactory factory){
        if (factory == null) {            
            factory = new BaseClassFactory();
            log("New class factory set to default factory.");
        } else {
            log("New class factory set to: " + factory.getClass().getName());
        }
        
        m_classFactory = factory;        
    }
    
    /**
     * Get the class factory
     * @return 
     */
    public IClassFactory getClassFactory(){
        return m_classFactory;
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());

        EditSession.ForceClassLoad();
        Operations.ForceClassLoad();
        
        s_instance = this;
        
        log("Enabled");
    }

    @Override
    public void onDisable() {
        
        s_instance = null;
        
        log("Disabled");
    }
}
