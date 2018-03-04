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
package org.primesoft.asyncworldedit.blockshub;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.configuration.IWorldEditConfig;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubBridge;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubFactory;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.inner.configuration.IConfigBlackList;
import org.primesoft.asyncworldedit.api.inner.configuration.IPremiumWorldEditConfig;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.AccessType;
import org.primesoft.asyncworldedit.configuration.BHLevel;
import org.primesoft.asyncworldedit.configuration.ConfigBlocksHub;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.platform.api.IPlatform;

/**
 *
 * @author SBPrime
 */
public class BlocksHubBridge implements IBlocksHubBridge {

    /**
     * The current integrator
     */
    private IBlocksHubIntegration m_integrator = new NullBlocksHubIntegration();

    /**
     * List of all factories
     */
    private final List<IBlocksHubFactory> m_factories = new ArrayList<IBlocksHubFactory>();

    /**
     * The platform
     */
    private final IPlatform m_platform;

    public BlocksHubBridge(IPlatform platform) {
        m_factories.add(new BlocksHubV2Factory());

        m_platform = platform;
    }

    @Override
    public void logBlock(IPlayerEntry playerEntry, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock, boolean dc) {
        BHLevel level = ConfigProvider.blocksHub().getLogBlocks();
        if (level == BHLevel.Disabled || (dc && level == BHLevel.Regular)) {
            return;
        }

        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return;
        }

        m_integrator.logBlock(playerEntry, world, location, oldBlock, newBlock, dc);
    }

    @Override
    public void addFacroty(IBlocksHubFactory factory) {
        synchronized (m_factories) {
            if (factory == null || m_factories.contains(factory)) {
                return;
            }

            m_factories.add(factory);
        }
    }

    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location) {
        ConfigBlocksHub bhConfig = ConfigProvider.blocksHub();

        BHLevel level = bhConfig.getCheckAccess();
        if (level == BHLevel.Disabled) {
            return true;
        }

        if (playerEntry == null) {
            return bhConfig.isAccessAllowed(AccessType.Null);
        }

        boolean isUnknown = playerEntry.isUnknown()
                || playerEntry.getName() == null || playerEntry.getName().isEmpty()
                || playerEntry.getUUID() == null || !playerEntry.isPlayer();

        boolean isConsole = playerEntry.isConsole();
        boolean isOffline = playerEntry.isPlayer() && playerEntry.isDisposed();

        if (isUnknown) {
            return bhConfig.isAccessAllowed(AccessType.Unknown);
        }
        if (isConsole) {
            return bhConfig.isAccessAllowed(AccessType.Console);
        }

        if (isOffline) {
            return bhConfig.isAccessAllowed(AccessType.Offline);
        }

        return m_integrator.hasAccess(playerEntry, world, location);
    }

    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location, boolean dc) {
        ConfigBlocksHub bhConfig = ConfigProvider.blocksHub();

        BHLevel level = bhConfig.getCheckAccess();
        if (level == BHLevel.Disabled || (dc && level == BHLevel.Regular)) {
            return true;
        }

        if (playerEntry == null) {
            return bhConfig.isAccessAllowed(AccessType.Null);
        }

        boolean isUnknown = playerEntry.isUnknown()
                || playerEntry.getName() == null || playerEntry.getName().isEmpty()
                || playerEntry.getUUID() == null || !playerEntry.isPlayer();

        boolean isConsole = playerEntry.isConsole();
        boolean isOffline = playerEntry.isPlayer() && playerEntry.isDisposed();

        if (isUnknown) {
            return bhConfig.isAccessAllowed(AccessType.Unknown);
        }
        if (isConsole) {
            return bhConfig.isAccessAllowed(AccessType.Console);
        }

        if (isOffline) {
            return bhConfig.isAccessAllowed(AccessType.Offline);
        }

        return m_integrator.hasAccess(playerEntry, world, location, dc);
    }

    /**
     * Check if player can place a block using WorldEdit black list
     *
     * @param playerEntry
     * @param newBlock
     * @return
     */
    private boolean canPlace(IPlayerEntry playerEntry, BaseBlock newBlock) {
        if (playerEntry == null || newBlock == null || playerEntry.isAllowed(Permission.BYPASS_WHITELIST)) {
            return true;
        }

        IPermissionGroup group = playerEntry.getPermissionGroup();
        if (group == null) {
            return true;
        }

        IWorldEditConfig weConfig = group.getWorldEditConfig();
        if (!(weConfig instanceof IPremiumWorldEditConfig)) {
            return true;
        }

        IConfigBlackList blackListConfig = ((IPremiumWorldEditConfig) weConfig).getBlockListOptions();
        if (blackListConfig == null) {
            return true;
        }

        if (!blackListConfig.isSetEnabled()) {
            return true;
        }

        Set<Integer> blackList = weConfig.getDisallowedBlocks();

        if (blackList == null) {
            if (m_platform == null) {
                return true;
            }

            IWorldeditIntegratorInner worldEdit = m_platform.getWorldEditIntegrator();
            if (worldEdit == null) {
                return true;
            }
            
            LocalConfiguration config = worldEdit.getConfiguration();
            if (config == null) {
                return true;
            }

            blackList = config.disallowedBlocks;
            if (blackList == null) {
                return true;
            }
        }

        if (blackList.isEmpty()) {
            return true;
        }

        return !blackList.contains(newBlock.getType());
    }

    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock) {        
        if (!canPlace(playerEntry, newBlock)) {
            return false;
        }
        
        if (playerEntry != null && playerEntry.isAllowed(Permission.BYPASS_BLOCKS_HUB)) {
            return true;
        }
        

        ConfigBlocksHub bhConfig = ConfigProvider.blocksHub();

        BHLevel level = bhConfig.getCheckAccess();
        if (level == BHLevel.Disabled) {
            return true;
        }

        if (playerEntry == null) {
            return bhConfig.isAccessAllowed(AccessType.Null);
        }

        boolean isUnknown = playerEntry.isUnknown()
                || playerEntry.getName() == null || playerEntry.getName().isEmpty()
                || playerEntry.getUUID() == null || !playerEntry.isPlayer();

        boolean isConsole = playerEntry.isConsole();
        boolean isOffline = playerEntry.isPlayer() && playerEntry.isDisposed();

        if (isUnknown) {
            return bhConfig.isAccessAllowed(AccessType.Unknown);
        }
        if (isConsole) {
            return bhConfig.isAccessAllowed(AccessType.Console);
        }

        if (isOffline) {
            return bhConfig.isAccessAllowed(AccessType.Offline);
        }

        return m_integrator.canPlace(playerEntry, world, location, oldBlock, newBlock);
    }

    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock, boolean dc) {
        if (!canPlace(playerEntry, newBlock)) {
            return false;
        }
        
        if (playerEntry != null && playerEntry.isAllowed(Permission.BYPASS_BLOCKS_HUB)) {
            return true;
        }

        ConfigBlocksHub bhConfig = ConfigProvider.blocksHub();

        BHLevel level = bhConfig.getCheckAccess();
        if (level == BHLevel.Disabled || (dc && level == BHLevel.Regular)) {
            return true;
        }

        if (playerEntry == null) {
            return bhConfig.isAccessAllowed(AccessType.Null);
        }

        boolean isUnknown = playerEntry.isUnknown()
                || playerEntry.getName() == null || playerEntry.getName().isEmpty()
                || playerEntry.getUUID() == null || !playerEntry.isPlayer();

        boolean isConsole = playerEntry.isConsole();
        boolean isOffline = playerEntry.isPlayer() && playerEntry.isDisposed();

        if (isUnknown) {
            return bhConfig.isAccessAllowed(AccessType.Unknown);
        }
        if (isConsole) {
            return bhConfig.isAccessAllowed(AccessType.Console);
        }

        if (isOffline) {
            return bhConfig.isAccessAllowed(AccessType.Offline);
        }

        return m_integrator.canPlace(playerEntry, world, location, oldBlock, newBlock, dc);
    }

    @Override
    public void initialize(Object blocksHubPlugin) {
        if (blocksHubPlugin == null) {
            return;
        }

        log(String.format("Initializing BlocksHub using %1$s...", blocksHubPlugin.getClass().getName()));

        IBlocksHubFactory[] factories;
        synchronized (m_factories) {
            factories = m_factories.toArray(new IBlocksHubFactory[0]);
        }

        for (IBlocksHubFactory factory : factories) {
            IBlocksHubIntegration integrator = create(factory, blocksHubPlugin);

            if (integrator != null) {
                m_integrator = integrator;
                log(String.format("BlocksHub integrator set to %1$s", factory.getName()));
                return;
            }
        }
    }

    /**
     * Try to create a new new bridge
     *
     * @param factory
     * @param blocksHubPlugin
     * @return
     */
    private IBlocksHubIntegration create(IBlocksHubFactory factory, Object blocksHubPlugin) {
        if (factory == null || blocksHubPlugin == null) {
            return null;
        }

        try {
            log(String.format("Trying to use %1$s factory.", factory.getName()));
            return factory.create(blocksHubPlugin);
        } catch (Error ex) {
            //We can ignore this error.
            return null;
        }
    }
}
