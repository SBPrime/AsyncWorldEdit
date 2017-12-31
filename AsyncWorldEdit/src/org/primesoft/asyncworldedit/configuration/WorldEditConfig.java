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
package org.primesoft.asyncworldedit.configuration;

import com.sk89q.worldedit.LocalSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.primesoft.asyncworldedit.api.inner.configuration.IConfigBlackList;
import org.primesoft.asyncworldedit.api.inner.configuration.IPremiumWorldEditConfig;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
public class WorldEditConfig implements IPremiumWorldEditConfig {

    public static WorldEditConfig parse(IConfigurationSection config) {
        if (config == null) {
            return null;
        }

        HashSet<Integer> disallowedBlocks;
        if (config.contains("disallowedBlocks")) {
            List<Integer> tmp = config.getIntegerList("disallowedBlocks");
            disallowedBlocks = new HashSet<Integer>();

            if (tmp != null) {
                for (Integer o : tmp) {
                    int id = o;

                    if (!disallowedBlocks.contains(id)) {
                        disallowedBlocks.add(id);
                    }
                }
            }
        }
        else {
            disallowedBlocks = null;
        }

        IConfigurationSection blackListSection = config.getConfigurationSection("disallowedBlocksUsage");
        IConfigBlackList blackListConfig = blackListSection != null
                ? new ConfigBlackList(blackListSection) : null;

        return new WorldEditConfig(
                config.getInt("maxBlockChanged", -1),
                config.getInt("historySize", LocalSession.MAX_HISTORY_SIZE),
                disallowedBlocks, blackListConfig);
    }

    /**
     * The maximum number of blocks that can be changed
     */
    private final int m_blockChangeLimit;

    /**
     * The history size
     */
    private final int m_historySize;

    /**
     * The black list options
     */
    private final IConfigBlackList m_blackList;

    /**
     * List of all disallowed blocks
     */
    private final HashSet<Integer> m_disallowedBlocks;

    private WorldEditConfig(int maxBlockChanged, int historySize,
            HashSet<Integer> disallowedBlocks, IConfigBlackList blackListOptions) {

        m_blockChangeLimit = maxBlockChanged;
        m_historySize = historySize;

        m_disallowedBlocks = disallowedBlocks;
        m_blackList = blackListOptions;
    }

    /**
     * Get the maximum number of blocks that can be changed in an edit session.
     *
     * @return block change limit
     */
    @Override
    public int getBlockChangeLimit() {
        return m_blockChangeLimit;
    }

    @Override
    public Set<Integer> getDisallowedBlocks() {
        return m_disallowedBlocks;
    }

    @Override
    public int getHistorySize() {
        return m_historySize;
    }

    @Override
    public IConfigBlackList getBlockListOptions() {
        return m_blackList;
    }
}
