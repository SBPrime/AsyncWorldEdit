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

import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
class BukkitConfigurationSection implements IConfigurationSection {
    private final ConfigurationSection m_section;
    
    public BukkitConfigurationSection(ConfigurationSection section) {
        m_section = section;
    }
    
    @Override
    public String getName() {
        return m_section.getName();
    }

    @Override
    public IConfigurationSection getConfigurationSection(String node) {
        ConfigurationSection section = m_section.getConfigurationSection(node);
        
        if (section == null) {
            return null;
        }
        
        return new BukkitConfigurationSection(section);
    }

    @Override
    public boolean contains(String node) {
        return m_section.contains(node);
    }

    @Override
    public void set(String node, Object value) {
        m_section.set(node, value);
    }

    @Override
    public void createSection(String name) {
        m_section.createSection(name);
    }

    @Override
    public Object get(String node) {
        Object result = m_section.get(node);        
        return (result instanceof ConfigurationSection) ? new BukkitConfigurationSection((ConfigurationSection)result) : result;
    }

    @Override
    public Object get(String node, Object defaultValue) {
        return m_section.get(node, defaultValue);
    }

    @Override
    public boolean getBoolean(String node, boolean defaultValue) {
        return m_section.getBoolean(node, defaultValue);
    }

    @Override
    public int getInt(String node, int defaultValue) {
        return m_section.getInt(node, defaultValue);
    }

    @Override
    public long getLong(String node, long defaultValue) {
        return m_section.getLong(node, defaultValue);
    }

    @Override
    public String getString(String node, String defaultValue) {
        return m_section.getString(node, defaultValue);
    }

    @Override
    public List<Integer> getIntegerList(String node) {
        return m_section.getIntegerList(node);
    }

    @Override
    public List<String> getStringList(String node) {
        return m_section.getStringList(node);
    }

    @Override
    public Set<String> getSubNodes() {
        return m_section.getKeys(false);
    }
    
}
