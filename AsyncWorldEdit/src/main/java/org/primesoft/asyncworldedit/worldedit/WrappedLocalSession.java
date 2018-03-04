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

import org.primesoft.asyncworldedit.events.LocalSessionLimitChanged;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.configuration.IWorldEditConfig;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.IExtendedLocalSession;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.events.LimitChanged;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.command.tool.ToolWrapper;

/**
 *
 * @author SBPrime
 */
public class WrappedLocalSession extends LocalSession implements IExtendedLocalSession {

    /**
     * The history field
     */
    private static final Field s_fieldHistory = Reflection.findField(LocalSession.class, "history", "Unable to get LocalSession history field");

    /**
     * The history field
     */
    private static final Field s_fieldHistoryPointer = Reflection.findField(LocalSession.class, "historyPointer", "Unable to get LocalSession historyPointer field");

    private final LocalSession m_parrent;

    public static WrappedLocalSession wrap(LocalSession localSession) {
        if (localSession == null) {
            return null;
        }

        return new WrappedLocalSession(localSession);
    }

    /**
     * The session owner
     */
    private IPlayerEntry m_sessionOwner;
    
    /**
     * The event bus
     */
    private final EventBus m_eventBus;

    public LocalSession getParrent() {
        return m_parrent;
    }

    protected WrappedLocalSession(LocalSession parrent) {
        m_parrent = parrent;
        
        m_eventBus = AwePlatform.getInstance().getCore().getEventBus();
    }

    @Override
    public void clearHistory() {
        m_parrent.clearHistory();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        WrappedLocalSession result = wrap(m_parrent);
        result.setOwner(m_sessionOwner);
        return result;
    }

    @Override
    public boolean compareAndResetDirty() {
        return m_parrent.compareAndResetDirty();
    }

    @Override
    public EditSession createEditSession(LocalPlayer player) {
        return m_parrent.createEditSession(player);
    }

    @Override
    public EditSession createEditSession(Player player) {
        return m_parrent.createEditSession(player);
    }

    @Override
    public void describeCUI(Actor actor) {
        m_parrent.describeCUI(actor);
    }

    @Override
    public Calendar detectDate(String input) {
        return m_parrent.detectDate(input);
    }

    @Override
    public void disableSuperPickAxe() {
        m_parrent.disableSuperPickAxe();
    }

    @Override
    public void dispatchCUIEvent(Actor actor, CUIEvent event) {
        m_parrent.dispatchCUIEvent(actor, event);
    }

    @Override
    public void dispatchCUISelection(Actor actor) {
        m_parrent.dispatchCUISelection(actor);
    }

    @Override
    public void dispatchCUISetup(Actor actor) {
        m_parrent.dispatchCUISetup(actor);
    }

    @Override
    public void enableSuperPickAxe() {
        m_parrent.enableSuperPickAxe();
    }

    @Override
    public boolean equals(Object obj) {
        LocalSession other;

        if (obj instanceof WrappedLocalSession) {
            other = ((WrappedLocalSession) obj).m_parrent;
        } else if (obj instanceof LocalSession) {
            other = (LocalSession) obj;
        } else {
            return false;
        }

        return m_parrent.equals(other);
    }

    @Override
    public BlockBag getBlockBag(Player player) {
        return m_parrent.getBlockBag(player);
    }

    @Override
    public int getBlockChangeLimit() {
        return m_parrent.getBlockChangeLimit();
    }

    @Override
    public BrushTool getBrushTool(int item) throws InvalidToolBindException {
        Tool tool = getTool(item);

        if (tool == null || !(tool instanceof BrushTool)) {
            tool = new BrushTool("worldedit.brush.sphere");
            setTool(item, tool);
        }

        return (BrushTool) getTool(item);
    }

    @Override
    public int getCUIVersion() {
        return m_parrent.getCUIVersion();
    }

    @Override
    public ClipboardHolder getClipboard() throws EmptyClipboardException {
        return m_parrent.getClipboard();
    }

    @Override
    public RegionSelectorType getDefaultRegionSelector() {
        return m_parrent.getDefaultRegionSelector();
    }

    @Override
    public String getLastScript() {
        return m_parrent.getLastScript();
    }

    @Override
    public Mask getMask() {
        return m_parrent.getMask();
    }

    @Override
    public Vector getPlacementPosition(Player player) throws IncompleteRegionException {
        return m_parrent.getPlacementPosition(player);
    }

    @Override
    public Region getRegion() throws IncompleteRegionException {
        return m_parrent.getRegion();
    }

    @Override
    public RegionSelector getRegionSelector() {
        return m_parrent.getRegionSelector();
    }

    @Override
    public RegionSelector getRegionSelector(LocalWorld world) {
        return m_parrent.getRegionSelector(world);
    }

    @Override
    public RegionSelector getRegionSelector(World world) {
        return m_parrent.getRegionSelector(world);
    }

    @Override
    public Region getSelection(LocalWorld world) throws IncompleteRegionException {
        return m_parrent.getSelection(world);
    }

    @Override
    public Region getSelection(World world) throws IncompleteRegionException {
        return m_parrent.getSelection(world);
    }

    @Override
    public World getSelectionWorld() {
        return m_parrent.getSelectionWorld();
    }

    @Override
    public Snapshot getSnapshot() {
        return m_parrent.getSnapshot();
    }

    @Override
    public BlockTool getSuperPickaxe() {
        return m_parrent.getSuperPickaxe();
    }

    @Override
    public TimeZone getTimeZone() {
        return m_parrent.getTimeZone();
    }

    @Override
    public Tool getTool(int item) {
        return m_parrent.getTool(item);
    }

    @Override
    public void handleCUIInitializationMessage(String text) {
        m_parrent.handleCUIInitializationMessage(text);
    }

    @Override
    public boolean hasCUISupport() {
        return m_parrent.hasCUISupport();
    }

    @Override
    public boolean hasFastMode() {
        return m_parrent.hasFastMode();
    }

    @Override
    public boolean hasSuperPickAxe() {
        return m_parrent.hasSuperPickAxe();
    }

    @Override
    public int hashCode() {
        return m_parrent.hashCode();
    }

    @Override
    public boolean isDirty() {
        return m_parrent.isDirty();
    }

    @Override
    public boolean isRegionDefined() {
        return m_parrent.isRegionDefined();
    }

    @Override
    public boolean isSelectionDefined(LocalWorld world) {
        return m_parrent.isSelectionDefined(world);
    }

    @Override
    public boolean isSelectionDefined(World world) {
        return m_parrent.isSelectionDefined(world);
    }

    @Override
    public boolean isToolControlEnabled() {
        return m_parrent.isToolControlEnabled();
    }

    @Override
    public boolean isUsingInventory() {
        return m_parrent.isUsingInventory();
    }

    @Override
    public void postLoad() {
        m_parrent.postLoad();
    }

    @Override
    public EditSession redo(BlockBag newBlockBag, LocalPlayer player) {
        return m_parrent.redo(newBlockBag, player);
    }

    @Override
    public EditSession redo(BlockBag newBlockBag, Player player) {
        return m_parrent.redo(newBlockBag, player);
    }

    @Override
    public void remember(EditSession editSession) {
        IPlayerEntry owner = m_sessionOwner;

        IPermissionGroup permGroup = owner != null ? owner.getPermissionGroup() : null;
        IWorldEditConfig weConfig = permGroup != null ? permGroup.getWorldEditConfig() : null;

        if (weConfig == null || s_fieldHistory == null || s_fieldHistoryPointer == null) {
            m_parrent.remember(editSession);
            return;
        }

        LocalSession.MAX_HISTORY_SIZE = Integer.MAX_VALUE;

        m_parrent.remember(editSession);

        int maxHistory = weConfig.getHistorySize();
        List<EditSession> history = Reflection.get(m_parrent, List.class, s_fieldHistory, "Unable to get history");
        if (history == null) {
            return;
        }

        if (history.size() > maxHistory) {
            while (history.size() > maxHistory) {
                history.remove(0);
            }

            Reflection.set(m_parrent, s_fieldHistoryPointer, history.size(), "Unable to set history pointer, history corrupted!");
        }
    }

    @Override
    public void setBlockChangeLimit(int maxBlocksChanged) {
        int oldLimit = getBlockChangeLimit();
        
        m_parrent.setBlockChangeLimit(maxBlocksChanged);
        
        m_eventBus.post(new LocalSessionLimitChanged(this, m_sessionOwner, oldLimit, maxBlocksChanged));
    }

    @Override
    public void setCUISupport(boolean support) {
        m_parrent.setCUISupport(support);
    }

    @Override
    public void setCUIVersion(int cuiVersion) {
        m_parrent.setCUIVersion(cuiVersion);
    }

    @Override
    public void setClipboard(ClipboardHolder clipboard) {
        m_parrent.setClipboard(clipboard);
    }

    @Override
    public void setConfiguration(LocalConfiguration config) {
        m_parrent.setConfiguration(config);
    }

    @Override
    public void setDefaultRegionSelector(RegionSelectorType defaultSelector) {
        m_parrent.setDefaultRegionSelector(defaultSelector);
    }

    @Override
    public void setFastMode(boolean fastMode) {
        m_parrent.setFastMode(fastMode);
    }

    @Override
    public void setLastScript(String lastScript) {
        m_parrent.setLastScript(lastScript);
    }

    @Override
    public void setMask(com.sk89q.worldedit.masks.Mask mask) {
        m_parrent.setMask(mask);
    }

    @Override
    public void setMask(Mask mask) {
        m_parrent.setMask(mask);
    }

    @Override
    public void setRegionSelector(LocalWorld world, RegionSelector selector) {
        m_parrent.setRegionSelector(world, selector);
    }

    @Override
    public void setRegionSelector(World world, RegionSelector selector) {
        m_parrent.setRegionSelector(world, selector);
    }

    @Override
    public void setSnapshot(Snapshot snapshot) {
        m_parrent.setSnapshot(snapshot);
    }

    @Override
    public void setSuperPickaxe(BlockTool tool) {
        m_parrent.setSuperPickaxe(ToolWrapper.wrapPickaxe(tool));
    }

    @Override
    public void setTimezone(TimeZone timezone) {
        m_parrent.setTimezone(timezone);
    }

    @Override
    public void setTool(int item, Tool tool) throws InvalidToolBindException {
        m_parrent.setTool(item, ToolWrapper.wrapTool(tool));
    }

    @Override
    public void setToolControl(boolean toolControl) {
        m_parrent.setToolControl(toolControl);
    }

    @Override
    public void setUseInventory(boolean useInventory) {
        m_parrent.setUseInventory(useInventory);
    }

    @Override
    public void tellVersion(Actor player) {
        m_parrent.tellVersion(player);
    }

    @Override
    public boolean togglePlacementPosition() {
        return m_parrent.togglePlacementPosition();
    }

    @Override
    public boolean toggleSuperPickAxe() {
        return m_parrent.toggleSuperPickAxe();
    }

    @Override
    public EditSession undo(BlockBag newBlockBag, LocalPlayer player) {
        return m_parrent.undo(newBlockBag, player);
    }

    @Override
    public EditSession undo(BlockBag newBlockBag, Player player) {
        return m_parrent.undo(newBlockBag, player);
    }

    @Override
    public String toString() {
        return m_parrent.toString();
    }

    /**
     * Set the session owner
     *
     * @param owner
     */
    public void setOwner(IPlayerEntry owner) {
        m_sessionOwner = owner;
    }
    
    @Override
    public IPlayerEntry getOwner() {
        return m_sessionOwner;
    }

    @Override
    public int getHistoryPointer() {
        if (s_fieldHistoryPointer == null) {
            return -1;
        }
        
        
        return Reflection.get(m_parrent, int.class, s_fieldHistoryPointer, "Unable to get history pointer.");
    }

    @Override
    public List<EditSession> getHistory() {
        if (s_fieldHistoryPointer == null) {
            return null;
        }
                
        return Reflection.get(m_parrent, List.class, s_fieldHistory, "Unable to get history");                
    }    
}
