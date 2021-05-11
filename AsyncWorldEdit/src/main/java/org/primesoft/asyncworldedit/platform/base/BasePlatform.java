package org.primesoft.asyncworldedit.platform.base;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubBridge;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubFactory;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.inner.IClassScanner;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.core.ChunkWatch;
import org.primesoft.asyncworldedit.core.PhysicsWatch;
import org.primesoft.asyncworldedit.platform.api.ICommandManager;
import org.primesoft.asyncworldedit.platform.api.IMaterialLibrary;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.platform.api.IPlayerProvider;
import org.primesoft.asyncworldedit.platform.api.IScheduler;

public abstract class BasePlatform implements IPlatform {

    private final ICommandManager m_commandManager;

    private final IPlayerProvider m_playerProvider;

    private final IScheduler m_scheduler;

    private final PhysicsWatch m_physicsWatcher;

    private final ChunkWatch m_chunkWatcher;

    private final IMaterialLibrary m_materialLibrary;

    private final IMapUtils m_mapUtils;

    private IAsyncWorldEditCore m_aweCore;

    private IBlocksHubBridge m_blocksHub;

    private IWorldeditIntegratorInner m_weIntegrator;

    private final IClassScanner m_classScanner;

    private final Set<PlatformEventListener> m_listeners = new HashSet<>();

    protected BasePlatform(
            final IClassScanner classScanner,
            final IMapUtils mapUtils,
            final Function<Supplier<IAsyncWorldEditCore>, ICommandManager> commandManager,
            final IPlayerProvider playerProvider,
            final IScheduler scheduler,
            final PhysicsWatch physicsWatcher,
            final ChunkWatch chunkWatcher,
            final IMaterialLibrary materialLibrary) {

        m_classScanner = classScanner;
        m_mapUtils = mapUtils;
        m_commandManager = commandManager.apply(this::getCore);
        m_playerProvider = playerProvider;
        m_scheduler = scheduler;
        m_physicsWatcher = physicsWatcher;
        m_chunkWatcher = chunkWatcher;
        m_materialLibrary = materialLibrary;
    }

    @Override
    public void reloadConfig() {
        broadcastEvent(PlatformEvent.CONFIGURATION_RELOADED);
    }

    public abstract void initialize(IAsyncWorldEditCore core);

    public void initialize(
            final IAsyncWorldEditCore core,
            final Supplier<IBlocksHubFactory>... bhFactory) {

        m_aweCore = core;
        m_blocksHub = core.getBlocksHubBridge();

        Stream.of(bhFactory).forEach(f -> m_blocksHub.addFactory(f.get()));
    }

    protected void initializeWorldEdit(Function<IAsyncWorldEditCore, IWorldeditIntegratorInner> integratorFactory) {
        m_weIntegrator = integratorFactory.apply(m_aweCore);
    }

    @Override
    public void onEnable() {
        listenersCleanup();

        getPlayerProvider().registerEvents();

        m_physicsWatcher.registerEvents();
        m_chunkWatcher.registerEvents();

    }

    @Override
    public void onDisable() {
        getChunkWatcher().clear();

        IWorldeditIntegratorInner weIntegrator = m_weIntegrator;
        if (weIntegrator != null) {
            weIntegrator.queueStop();
        }

        listenersCleanup();
    }

    @Override
    public void registerEventListener(
            final PlatformEventListener listener) {

        m_listeners.add(listener);
    }

    private IAsyncWorldEditCore getCore() {
        return m_aweCore;
    }

    @Override
    public IMapUtils getMapUtils() {
        return m_mapUtils;
    }

    @Override
    public ICommandManager getCommandManager() {
        return m_commandManager;
    }

    @Override
    public IPlayerProvider getPlayerProvider() {
        return m_playerProvider;
    }


    @Override
    public IScheduler getScheduler() {
        return m_scheduler;
    }

    @Override
    public IPhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    @Override
    public IChunkWatch getChunkWatcher() {
        return m_chunkWatcher;
    }

    @Override
    public IMaterialLibrary getMaterialLibrary() {
        return m_materialLibrary;
    }

    @Override
    public IWorldeditIntegratorInner getWorldEditIntegrator() {
        return m_weIntegrator;
    }

    @Override
    public IClassScanner getClassScanner() {
        return m_classScanner;
    }

    private void listenersCleanup() {
        m_listeners.clear();
    }

    protected void initializeBlocksHub(final Object data) {
        m_blocksHub.initialize(data);
    }

    private void broadcastEvent(
            final PlatformEvent event) {

        m_listeners.forEach(i -> i.onPlatformEvent(event));
    }
}
