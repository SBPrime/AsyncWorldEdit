/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.core;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.util.eventbus.EventBus;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.commands.*;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.playerManager.PlayerManager;
import org.primesoft.asyncworldedit.api.IPlotMeFix;
import org.primesoft.asyncworldedit.plotme.NullFix;
import org.primesoft.asyncworldedit.strings.MessageProvider;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.taskdispatcher.TaskDispatcher;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.directChunk.relighter.BlockReligher;

import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IAdapter;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.adapter.AdapterProvider;
import org.primesoft.asyncworldedit.api.IAweOperations;
import org.primesoft.asyncworldedit.api.changesetSerializer.ISerializerManager;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.inner.IInnerSerializerManager;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.changesetSerializer.SerializerManager;
import org.primesoft.asyncworldedit.platform.api.ICommandManager;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.classScanner.IClassScannerOptions;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkCommands;
import org.primesoft.asyncworldedit.api.inner.IBlockRelighter;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubBridge;
import org.primesoft.asyncworldedit.api.inner.IInitializableAdapter;
import org.primesoft.asyncworldedit.api.inner.IInnerDirectChunkAPI;
import org.primesoft.asyncworldedit.blockshub.BlocksHubBridge;
import org.primesoft.asyncworldedit.excommands.chunk.DirectChunkCommands;
import org.primesoft.asyncworldedit.progressDisplay.ProgressDisplayManager;
import org.primesoft.asyncworldedit.versionChecker.VersionChecker;

/**
 * The plugin main class
 *
 * @author SBPrime
 */
public class AsyncWorldEditCore implements IAsyncWorldEditCore, IAweOperations {

    private Boolean m_isInitialized = false;
    private BlockPlacer m_blockPlacer;
    private TaskDispatcher m_dispatcher;
    private IPlotMeFix m_plotMeFix;
    private final PlayerManager m_playerManager = new PlayerManager(this);
    private IProgressDisplayManager m_progressDisplay;
    private InjectorBridge m_aweInjector;
    private IAdapter m_nativeAdapter;
    private IInnerSerializerManager m_changesetSerializer;
    private IDirectChunkCommands m_directChunkCommands;
    private final MessageProvider m_messageProvider;
    private final IBlocksHubBridge m_blocksHubBridge;
    
    /**
     * Current platform
     */
    private final IPlatform m_platform;

    public AsyncWorldEditCore(IPlatform platform) {
        m_platform = platform;
        m_messageProvider = m_platform.createMessageProvider();
        m_blocksHubBridge = new BlocksHubBridge(m_platform);

        log(String.format("Platform set to %1$s", platform.getName()));

        AwePlatform awePlatform = AwePlatform.getInstance();
        awePlatform.initialize(this);
    }

    @Override
    public void initialize() {
        m_platform.initialize(this);
        ICommandManager cm = m_platform.getCommandManager();
        cm.registerCommand("awe", new String[]{"/awe"},
                "Displays the help for Async world edit.",
                "/awe <sub command>", null);
    }

    @Override
    public IPlatform getPlatform() {
        return m_platform;
    }

    @Override
    public double getAPIVersion() {
        return 3.002000006;
    }

    @Override
    public final byte[] getDetailAPIVersion() {
        return new byte[]{2, 0, 6};
    }

    /**
     * Get teh native API adapter
     *
     * @return
     */
    @Override
    public IAdapter getAdapter() {
        return m_nativeAdapter;
    }

    @Override
    public IAweOperations getOperations() {
        return this;
    }

    @Override
    public IDirectChunkCommands getChunkOperations() {
        return m_directChunkCommands;
    }
    
    /**
     * Get the direct chunk API
     *
     * @return
     */
    @Override
    public IDirectChunkAPI getDirectChunkAPI() {
        return (m_nativeAdapter != null)
                ? m_nativeAdapter.getDirectChunkAPI() : null;
    }

    /**
     * Get the direct chunk API
     *
     * @return
     */
    @Override
    public IInnerDirectChunkAPI getInnerDirectChunkAPI() {
        return (IInnerDirectChunkAPI) getDirectChunkAPI();
    }

    /**
     * Get the map manipulation utils
     *
     * @return
     */
    @Override
    public IMapUtils getMapUtils() {
        return m_platform.getMapUtils();
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return m_playerManager;
    }

    @Override
    public IPhysicsWatch getPhysicsWatcher() {
        return m_platform.getPhysicsWatcher();
    }

    @Override
    public IChunkWatch getChunkWatch() {
        return m_platform.getChunkWatcher();
    }

    @Override
    public IPlotMeFix getPlotMeFix() {
        return m_plotMeFix;
    }

    @Override
    public IWorldeditIntegratorInner getWorldEditIntegrator() {
        return m_platform.getWorldEditIntegrator();
    }
    
    @Override
    public EventBus getEventBus() {
        IWorldeditIntegratorInner we = m_platform.getWorldEditIntegrator();
        return we != null ? we.getEventBus() : null;
    }

    @Override
    public void setPlotMeFix(IPlotMeFix plotMeFix) {
        if (plotMeFix == null) {
            plotMeFix = new NullFix();
        }

        log(String.format("PlotMeFix set to %1$s.", plotMeFix.getClass()));
        m_plotMeFix = plotMeFix;
    }

    @Override
    public IBlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    @Override
    public ITaskDispatcher getTaskDispatcher() {
        return m_dispatcher;
    }

    @Override
    public IProgressDisplayManager getProgressDisplayManager() {
        return m_progressDisplay;
    }

    @Override
    public IBlocksHubBridge getBlocksHubBridge() {
        return m_blocksHubBridge;
    }

    @Override
    public ISerializerManager getChangesetSerializer() {
        return m_changesetSerializer;
    }

    @Override
    public IInnerSerializerManager getInnerChangesetSerializer() {
        return m_changesetSerializer;
    }

    @Override
    public IWorld getWorld(UUID worldUUID) {
        return m_platform.getWorld(worldUUID);
    }

    @Override
    public IWorld getWorld(String worldName) {
        return m_platform.getWorld(worldName);
    }

    @Override
    public IClassScannerOptions getClassScannerOptions() {
        return m_platform.getClasScanner();
    }

    @Override
    public void onEnable() {
        m_isInitialized = false;
        m_platform.onEnable();

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
            return;
        }

        initialiseStrings();

        m_progressDisplay = new ProgressDisplayManager();

        m_dispatcher = new TaskDispatcher(this);
        m_blockPlacer = new BlockPlacer(this);

        m_changesetSerializer = new SerializerManager(this);

        m_nativeAdapter = AdapterProvider.get(m_platform);
        if (m_nativeAdapter != null) {

            if (m_nativeAdapter instanceof IInitializableAdapter) {
                ((IInitializableAdapter) m_nativeAdapter).initialize(m_dispatcher, m_blocksHubBridge);
            }
            m_directChunkCommands = new DirectChunkCommands(this);
        } else {
            m_directChunkCommands = null;
        }

        setPlotMeFix(new NullFix());

        try {
            m_aweInjector = InjectorBridge.initialize(this);
        } catch (Error ex) {
            ExceptionHelper.printException(ex, "AsyncWorldEditInjector not found.");
            m_aweInjector = null;
        }

        if (m_aweInjector == null) {
            log("Unable to get instance of AsyncWorldEdit Injector. AsyncWorldEdit disabled.");
            return;
        }

        if (ConfigProvider.getCheckUpdate()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    log(VersionChecker.CheckVersion(m_platform.getVersion()).getMessage());

                }
            }).start();
        }

        if (!ConfigProvider.isConfigUpdated()) {
            log("Please update your config file!");
        }

        m_platform.getChunkWatcher().setTaskDispat(m_dispatcher);

        IInnerDirectChunkAPI dcApi = getInnerDirectChunkAPI();
        IBlockRelighter bRelighter = dcApi != null ? dcApi.getBlockRelighter() : null;
        if (bRelighter != null) {
            bRelighter.initialize(m_platform);
        }

        m_isInitialized = true;

        initializeConfig();

        m_platform.getPlayerProvider().initialize(m_playerManager);

        //testSerializer();
    }

    private void testSerializer() {
        int cnt = 100000;
        ISerializerManager sm = new SerializerManager(this);
        List<Change> change = new ArrayList<Change>(cnt);

        for (int i = 0; i < cnt; i++) {
            change.add(new BlockChange(new BlockVector(i, i % 3, i % 7), new BaseBlock(i % 255, 0), new BaseBlock(i % 255, 1)));
        }

        UUID uuid = new UUID(0, 0);
        IPlayerEntry player = getPlayerManager().createFakePlayer("FAKE", uuid);
        File file = sm.open(player, 0);

        long now1 = System.currentTimeMillis();
        sm.save(file, change);
        long now2 = System.currentTimeMillis();

        System.out.println("SAVE: " + (now2 - now1));

        Change[] tmp;
        now1 = System.currentTimeMillis();
        List<Change> list = sm.load(file, cnt, player, null);
        tmp = list.toArray(new Change[0]);
        now2 = System.currentTimeMillis();
        System.out.println("LOAD: " + (now2 - now1));

        now1 = System.currentTimeMillis();
        tmp = Lists.reverse(list).toArray(new Change[0]);
        now2 = System.currentTimeMillis();
        System.out.println("INVERSE: " + (now2 - now1));

        sm.close(file);

        System.out.println("SIZE: " + list.size());
        for (int i = 0; i < cnt; i++) {
            BlockChange c = (BlockChange) list.get(i);

            BlockVector vector = c.getPosition();
            int x = vector.getBlockX();
            int y = vector.getBlockY();
            int z = vector.getBlockZ();

            BaseBlock before = c.getPrevious();
            BaseBlock current = c.getCurrent();

            boolean isOk = x == i && y == (i % 3) && z == (i % 7);
            isOk &= before.getId() == i % 255;
            isOk &= current.getId() == i % 255;

            isOk &= before.getData() == 0 && current.getData() == 1;
            if (!isOk) {
                System.out.println("ERROR: " + i);
            }
        }
    }

    /**
     * Initialize the plugin in aordance to the config
     */
    private void initializeConfig() {
        m_blockPlacer.loadConfig();
        if (ConfigProvider.isPhysicsFreezEnabled()) {
            m_platform.getPhysicsWatcher().enable();
        } else {
            m_platform.getPhysicsWatcher().disable();
        }
    }

    /**
     * Initialise the strings
     */
    private void initialiseStrings() {
        if (!MessageProvider.saveDefault()) {
            log("Unable to save english.yml to plugin folder.");
        }

        if (!m_messageProvider.loadDefault()) {
            log("Error loading default strings file, no internal fallback available!.");
        }
        if (!m_messageProvider.loadFile(ConfigProvider.getStringsFile())) {
            log("Error loading strings file, using internal fallback.");
        }
    }

    @Override
    public void onDisable() {
        if (m_isInitialized) {
            m_blockPlacer.stop();
            m_dispatcher.stop();

            IInnerDirectChunkAPI dcApi = getInnerDirectChunkAPI();
            IBlockRelighter bRelighter = dcApi != null ? dcApi.getBlockRelighter() : null;
            if (bRelighter != null) {
                bRelighter.stop();
            }

            m_isInitialized = false;

            m_platform.onDisable();
        }
    }

    @Override
    public boolean onCommand(IPlayerEntry player, String commandName, String[] args) {
        if (!commandName.equalsIgnoreCase(Commands.COMMAND_MAIN)) {
            return false;
        }

        String name = (args != null && args.length > 0) ? args[0] : "";

        if (name.equalsIgnoreCase(Commands.COMMAND_RELOAD)) {
            doReloadConfig(player, args != null && args.length > 1 ? args[1] : "");
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_HELP)) {
            String arg = args != null && args.length > 1 ? args[1] : null;
            return Help.ShowHelp(player, arg);
        } else if (name.equalsIgnoreCase(Commands.COMMAND_PURGE)) {
            doPurge(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_JOBS)) {
            doJobs(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_CANCEL)) {
            doCancel(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_TOGGLE)) {
            doToggle(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_UNDO)) {
            doToggleUndo(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_SPEED)) {
            doSpeed(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_MESSAGES)) {
            doMessages(player, args);
            return true;
        }

        return Help.ShowHelp(player, null);
    }

    /**
     * Reload the plugin configuration
     *
     * @param player
     * @param arg
     */
    private void doReloadConfig(IPlayerEntry player, String arg) {
        if (!player.isAllowed(Permission.RELOAD_CONFIG)) {
            player.say(MessageType.NO_PERMS.format());
            return;
        }

        if (arg == null || arg.length() == 0) {
            Help.ShowHelp(player, Commands.COMMAND_RELOAD);
            return;
        }

        boolean reloadConfig, flushGroups;

        if (arg.equalsIgnoreCase("all")) {
            reloadConfig = true;
            flushGroups = true;
        } else if (arg.equalsIgnoreCase("config")) {
            reloadConfig = true;
            flushGroups = false;
        } else if (arg.equalsIgnoreCase("groups")) {
            reloadConfig = false;
            flushGroups = true;
        } else {
            Help.ShowHelp(player, Commands.COMMAND_RELOAD);
            return;
        }

        log(String.format("%1$s reloading config (%2$s)...", player.getName(), arg));
        if (reloadConfig) {
            m_platform.reloadConfig();
            m_isInitialized = false;

            if (!ConfigProvider.load(this)) {
                player.say(MessageType.CMD_RELOAD_ERROR.format());
                return;
            }

            if (!m_messageProvider.loadFile(ConfigProvider.getStringsFile())) {
                log("Error loading strings file, using internal fallback.");
            }
        }

        if (flushGroups) {
            m_playerManager.updateGroups();
        }
        if (reloadConfig) {
            initializeConfig();
        }

        m_isInitialized = true;
        player.say(MessageType.CMD_RELOAD_DONE.format());
    }

    /**
     * Perform the messages command
     *
     * @param player
     * @param args
     */
    private void doMessages(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        MessagesCommand.Execte(this, player, args);
    }

    /**
     * Perform the toggle command
     *
     * @param player
     * @param args
     */
    private void doToggle(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        ToggleCommand.Execte(this, player, args);
    }

    /**
     * Perform the undo toggle command
     *
     * @param player
     * @param args
     */
    private void doToggleUndo(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        UndoCommand.Execte(this, player, args);
    }

    /**
     * Perform the speed command
     *
     * @param player
     * @param args
     */
    private void doSpeed(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        SpeedCommand.Execte(this, player, args);
    }

    /**
     * Perform the purge command
     *
     * @param player
     * @param args
     */
    private void doPurge(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        PurgeCommand.Execte(this, player, args);
    }

    /**
     * Perform the job command
     *
     * @param player
     * @param args
     */
    private void doJobs(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        JobsCommand.Execte(this, player, args);
    }

    /**
     * Perform the cancel command
     *
     * @param player
     * @param args
     */
    private void doCancel(IPlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        CancelCommand.Execte(this, player, args);
    }
}
