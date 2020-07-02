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
package org.primesoft.asyncworldedit.worldedit;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.configuration.IWorldEditConfig;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.IExtendedLocalSession;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.events.LocalSessionLimitChanged;
import org.primesoft.asyncworldedit.injector.injected.ILocalSession;
import org.primesoft.asyncworldedit.worldedit.command.tool.ToolWrapper;

/**
 * @author SBPrime
 */
public class WrappedLocalSession extends LocalSession implements IExtendedLocalSession {
    private final LocalSession m_parent;
    /**
     * The event bus
     */
    private final EventBus m_eventBus;
    /**
     * The session owner
     */
    private IPlayerEntry m_sessionOwner;

    protected WrappedLocalSession(LocalSession parent) {
        m_parent = parent;

        if (!(parent instanceof WrappedLocalSession)) {
            wrapTools(((ILocalSession) parent).getTools());
        }

        m_eventBus = AwePlatform.getInstance().getCore().getEventBus();
    }

    public static WrappedLocalSession wrap(LocalSession localSession) {
        if (localSession == null) {
            return null;
        }

        return new WrappedLocalSession(localSession);
    }

    public LocalSession getParent() {
        return m_parent;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        WrappedLocalSession result = wrap(m_parent);
        result.setOwner(m_sessionOwner);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        LocalSession other;

        if (obj instanceof WrappedLocalSession) {
            other = ((WrappedLocalSession) obj).m_parent;
        } else if (obj instanceof LocalSession) {
            other = (LocalSession) obj;
        } else {
            return false;
        }

        return m_parent.equals(other);
    }

    @Override
    public BrushTool getBrushTool(ItemType item) throws InvalidToolBindException {
        Tool tool = getTool(item);

        if (tool == null || !(tool instanceof BrushTool)) {
            tool = new BrushTool("worldedit.brush.sphere");
            setTool(item, tool);
        }

        return (BrushTool) getTool(item);
    }


    private void wrapTools(Map<ItemType, Tool> tools) {
        if (tools == null) {
            return;
        }

        ItemType[] items = tools.keySet().toArray(new ItemType[0]);
        for (ItemType item : items) {
            Tool tool = tools.get(item);

            if (tool == null) {
                continue;
            }

            tools.put(item, ToolWrapper.wrapTool(tool));
        }
    }

    @Override
    public IPlayerEntry getOwner() {
        return m_sessionOwner;
    }

    /**
     * Set the session owner
     */
    public void setOwner(IPlayerEntry owner) {
        m_sessionOwner = owner;
    }

    @Override
    public void remember(EditSession editSession) {
        IPlayerEntry owner = m_sessionOwner;

        IPermissionGroup permGroup = owner != null ? owner.getPermissionGroup() : null;
        IWorldEditConfig weConfig = permGroup != null ? permGroup.getWorldEditConfig() : null;

        if (weConfig == null) {
            m_parent.remember(editSession);
            return;
        }

        LocalSession.MAX_HISTORY_SIZE = Integer.MAX_VALUE;

        m_parent.remember(editSession);

        int maxHistory = weConfig.getHistorySize();
        List<EditSession> history = ((ILocalSession) m_parent).getHistory();
        if (history == null) {
            return;
        }

        if (history.size() > maxHistory) {
            while (history.size() > maxHistory) {
                history.remove(0);
            }

            ((ILocalSession) m_parent).setHistoryPointer(history.size());
        }
    }

    @Override
    public void clearHistory() {
        m_parent.clearHistory();
    }
    //------------------------------------------------

    @Override
    public boolean compareAndResetDirty() {
        return m_parent.compareAndResetDirty();
    }

    @Override
    public EditSession createEditSession(Player player) {
        return m_parent.createEditSession(player);
    }

    @Override
    public EditSession createEditSession(Actor actor) {
        return m_parent.createEditSession(actor);
    }

    @Override
    public void describeCUI(Actor actor) {
        m_parent.describeCUI(actor);
    }

    @Override
    public Calendar detectDate(String input) {
        return m_parent.detectDate(input);
    }

    @Override
    public void disableSuperPickAxe() {
        m_parent.disableSuperPickAxe();
    }

    @Override
    public void dispatchCUIEvent(Actor actor, CUIEvent event) {
        m_parent.dispatchCUIEvent(actor, event);
    }

    @Override
    public void dispatchCUISelection(Actor actor) {
        m_parent.dispatchCUISelection(actor);
    }

    @Override
    public void dispatchCUISetup(Actor actor) {
        m_parent.dispatchCUISetup(actor);
    }

    @Override
    public void enableSuperPickAxe() {
        m_parent.enableSuperPickAxe();
    }

    @Override
    public BlockBag getBlockBag(Player player) {
        return m_parent.getBlockBag(player);
    }

    @Override
    public int getBlockChangeLimit() {
        return m_parent.getBlockChangeLimit();
    }

    @Override
    public void setBlockChangeLimit(int maxBlocksChanged) {
        int oldLimit = getBlockChangeLimit();

        m_parent.setBlockChangeLimit(maxBlocksChanged);

        m_eventBus.post(new LocalSessionLimitChanged(this, m_sessionOwner, oldLimit, maxBlocksChanged));
    }

    @Override
    public int getCUIVersion() {
        return m_parent.getCUIVersion();
    }

    @Override
    public void setCUIVersion(int cuiVersion) {
        m_parent.setCUIVersion(cuiVersion);
    }

    @Override
    public ClipboardHolder getClipboard() throws EmptyClipboardException {
        return m_parent.getClipboard();
    }

    @Override
    public void setClipboard(ClipboardHolder clipboard) {
        m_parent.setClipboard(clipboard);
    }

    @Override
    public RegionSelectorType getDefaultRegionSelector() {
        return m_parent.getDefaultRegionSelector();
    }

    @Override
    public void setDefaultRegionSelector(RegionSelectorType defaultSelector) {
        m_parent.setDefaultRegionSelector(defaultSelector);
    }

    @Override
    public String getLastScript() {
        return m_parent.getLastScript();
    }

    @Override
    public void setLastScript(String lastScript) {
        m_parent.setLastScript(lastScript);
    }

    @Override
    public Mask getMask() {
        return m_parent.getMask();
    }

    @Override
    public void setMask(Mask mask) {
        m_parent.setMask(mask);
    }

    @Override
    public BlockVector3 getPlacementPosition(Player player) throws IncompleteRegionException {
        return m_parent.getPlacementPosition(player);
    }

    @Override
    public BlockVector3 getPlacementPosition(Actor actor) throws IncompleteRegionException {
        return m_parent.getPlacementPosition(actor);
    }

    @Override
    public RegionSelector getRegionSelector(World world) {
        return m_parent.getRegionSelector(world);
    }

    @Override
    public Region getSelection(World world) throws IncompleteRegionException {
        return m_parent.getSelection(world);
    }

    @Override
    public World getSelectionWorld() {
        return m_parent.getSelectionWorld();
    }

    @Override
    public Snapshot getSnapshot() {
        return m_parent.getSnapshot();
    }

    @Override
    public void setSnapshot(Snapshot snapshot) {
        m_parent.setSnapshot(snapshot);
    }

    @Override
    public BlockTool getSuperPickaxe() {
        return m_parent.getSuperPickaxe();
    }

    @Override
    public void setSuperPickaxe(BlockTool tool) {
        m_parent.setSuperPickaxe(ToolWrapper.wrapPickaxe(tool));
    }

    @Override
    public ZoneId getTimeZone() {
        return m_parent.getTimeZone();
    }

    @Override
    public Tool getTool(ItemType item) {
        return m_parent.getTool(item);
    }

    @Override
    public void handleCUIInitializationMessage(String text, Actor actor) {
        m_parent.handleCUIInitializationMessage(text, actor);
    }

    @Override
    public boolean hasCUISupport() {
        return m_parent.hasCUISupport();
    }

    @Override
    public boolean hasFastMode() {
        return m_parent.hasFastMode();
    }

    @Override
    public boolean hasSuperPickAxe() {
        return m_parent.hasSuperPickAxe();
    }

    @Override
    public int hashCode() {
        return m_parent.hashCode();
    }

    @Override
    public boolean isDirty() {
        return m_parent.isDirty();
    }

    @Override
    public boolean isSelectionDefined(World world) {
        return m_parent.isSelectionDefined(world);
    }

    @Override
    public boolean isToolControlEnabled() {
        return m_parent.isToolControlEnabled();
    }

    @Override
    public boolean isUsingInventory() {
        return m_parent.isUsingInventory();
    }

    @Override
    public void postLoad() {
        m_parent.postLoad();
    }

    @Override
    public EditSession redo(BlockBag newBlockBag, Player player) {
        return m_parent.redo(newBlockBag, player);
    }

    @Override
    public EditSession redo(BlockBag newBlockBag, Actor actor) {
        return m_parent.redo(newBlockBag, actor);
    }

    @Override
    public void setCUISupport(boolean support) {
        m_parent.setCUISupport(support);
    }

    @Override
    public void setConfiguration(LocalConfiguration config) {
        m_parent.setConfiguration(config);
    }

    @Override
    public void setFastMode(boolean fastMode) {
        m_parent.setFastMode(fastMode);
    }

    @Override
    public void setRegionSelector(World world, RegionSelector selector) {
        m_parent.setRegionSelector(world, selector);
    }

    @Override
    public void setTimezone(ZoneId timezone) {
        m_parent.setTimezone(timezone);
    }

    @Override
    public void setTool(ItemType item, Tool tool) throws InvalidToolBindException {
        m_parent.setTool(item, ToolWrapper.wrapTool(tool));
    }

    @Override
    public void setToolControl(boolean toolControl) {
        m_parent.setToolControl(toolControl);
    }

    @Override
    public void setUseInventory(boolean useInventory) {
        m_parent.setUseInventory(useInventory);
    }

    @Override
    public void tellVersion(Actor player) {
        m_parent.tellVersion(player);
    }

    @Override
    public boolean togglePlacementPosition() {
        return m_parent.togglePlacementPosition();
    }

    @Override
    public boolean toggleSuperPickAxe() {
        return m_parent.toggleSuperPickAxe();
    }

    @Override
    public EditSession undo(BlockBag newBlockBag, Player player) {
        return m_parent.undo(newBlockBag, player);
    }

    @Override
    public EditSession undo(BlockBag newBlockBag, Actor actor) {
        return m_parent.undo(newBlockBag, actor);
    }


    @Override
    public String toString() {
        return m_parent.toString();
    }

    @Override
    public int getHistoryPointer() {
        return ((ILocalSession) m_parent).getHistoryPointer();
    }

    @Override
    public List<EditSession> getHistory() {
        return ((ILocalSession) m_parent).getHistory();
    }

    @Override
    public boolean shouldUseServerCUI() {
        return m_parent.shouldUseServerCUI();
    }

    @Override
    public void setUseServerCUI(boolean useServerCUI) {
        m_parent.setUseServerCUI(useServerCUI);
    }

    @Override
    public void updateServerCUI(Actor actor) {
        m_parent.updateServerCUI(actor);
    }

    @Override
    public EditSession.ReorderMode getReorderMode() {
        return m_parent.getReorderMode();
    }

    @Override
    public void setReorderMode(EditSession.ReorderMode rm) {
        m_parent.setReorderMode(rm);
    }

    @Override
    public int getTimeout() {
        return m_parent.getTimeout();
    }

    @Override
    public void setTimeout(int i) {
        m_parent.setTimeout(i);
    }

    public boolean hasWorldOverride() {
        return m_parent.hasWorldOverride();
    }

    @Nullable
    public World getWorldOverride() {
        return m_parent.getWorldOverride();
    }

    public void setWorldOverride(@Nullable World worldOverride) {
        m_parent.setWorldOverride(worldOverride);
    }

    public boolean isTickingWatchdog() {
        return m_parent.isTickingWatchdog();
    }

    public void setTickingWatchdog(boolean tickingWatchdog) {
        m_parent.setTickingWatchdog(tickingWatchdog);
    }

    public boolean isPlaceAtPos1() {
        return m_parent.isPlaceAtPos1();
    }

    public void setPlaceAtPos1(boolean placeAtPos1) {
        m_parent.setPlaceAtPos1(placeAtPos1);
    }

    @Nullable
    public com.sk89q.worldedit.world.snapshot.experimental.Snapshot getSnapshotExperimental() {
        return m_parent.getSnapshotExperimental();
    }

    public void setSnapshotExperimental(@Nullable com.sk89q.worldedit.world.snapshot.experimental.Snapshot snapshotExperimental) {
        m_parent.setSnapshotExperimental(snapshotExperimental);
    }

    public SideEffectSet getSideEffectSet() {
        return m_parent.getSideEffectSet();
    }

    public void setSideEffectSet(SideEffectSet sideEffectSet) {
        m_parent.setSideEffectSet(sideEffectSet);
    }

    public String getWandItem() {
        return m_parent.getWandItem();
    }

    public String getNavWandItem() {
        return m_parent.getNavWandItem();
    }

    public List<Countable<BlockState>> getLastDistribution() {
        return m_parent.getLastDistribution();
    }

    public void setLastDistribution(List<Countable<BlockState>> dist) {
        m_parent.setLastDistribution(dist);
    }
}
