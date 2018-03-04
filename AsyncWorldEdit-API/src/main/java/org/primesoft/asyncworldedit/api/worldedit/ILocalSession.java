/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2017, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
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
package org.primesoft.asyncworldedit.api.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
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
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import java.util.Calendar;
import java.util.TimeZone;
import javax.annotation.Nullable;

/**
 *
 * @author SBPrime
 */
interface ILocalSession {
    /**
     * Set the configuration.
     *
     * @param config the configuration
     */
    public void setConfiguration(LocalConfiguration config);

    /**
     * Called on post load of the session from persistent storage.
     */
    public void postLoad();

    /**
     * Get whether this session is "dirty" and has changes that needs to
     * be committed.
     *
     * @return true if dirty
     */
    public boolean isDirty();

    /**
     * Get whether this session is "dirty" and has changes that needs to
     * be committed, and reset it to {@code false}.
     *
     * @return true if the dirty value was {@code true}
     */
    public boolean compareAndResetDirty();

    /**
     * Get the session's timezone.
     *
     * @return the timezone
     */
    public TimeZone getTimeZone();

    /**
     * Set the session's timezone.
     *
     * @param timezone the user's timezone
     */
    public void setTimezone(TimeZone timezone);

    /**
     * Clear history.
     */
    public void clearHistory();

    /**
     * Remember an edit session for the undo history. If the history maximum
     * size is reached, old edit sessions will be discarded.
     *
     * @param editSession the edit session
     */
    public void remember(EditSession editSession);
    
    /**
     * Performs an undo.
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was undone
     */
    public EditSession undo(@Nullable BlockBag newBlockBag, LocalPlayer player);
    
    /**
     * Performs an undo.
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was undone
     */
    public EditSession undo(@Nullable BlockBag newBlockBag, Player player);

    /**
     * Performs a redo
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was redone
     */
    public EditSession redo(@Nullable BlockBag newBlockBag, LocalPlayer player);

    /**
     * Performs a redo
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was redone
     */
    public EditSession redo(@Nullable BlockBag newBlockBag, Player player);

    /**
     * Get the default region selector.
     *
     * @return the default region selector
     */
    public RegionSelectorType getDefaultRegionSelector();

    /**
     * Set the default region selector.
     *
     * @param defaultSelector the default region selector
     */
    public void setDefaultRegionSelector(RegionSelectorType defaultSelector);

    /**
     * @deprecated Use {@link #getRegionSelector(World)}
     */
    @Deprecated
    public RegionSelector getRegionSelector(LocalWorld world);

    /**
     * Get the region selector for defining the selection. If the selection
     * was defined for a different world, the old selection will be discarded.
     *
     * @param world the world
     * @return position the position
     */
    public RegionSelector getRegionSelector(World world);

    /**
     * @deprecated use {@link #getRegionSelector(World)}
     */
    @Deprecated
    public RegionSelector getRegionSelector();

    /**
     * @deprecated use {@link #setRegionSelector(World, RegionSelector)}
     */
    @Deprecated
    public void setRegionSelector(LocalWorld world, RegionSelector selector);

    /**
     * Set the region selector.
     *
     * @param world the world
     * @param selector the selector
     */
    public void setRegionSelector(World world, RegionSelector selector);

    /**
     * Returns true if the region is fully defined.
     *
     * @return true if a region selection is defined
     */
    @Deprecated
    public boolean isRegionDefined();

    /**
     * @deprecated use {@link #isSelectionDefined(World)}
     */
    @Deprecated
    public boolean isSelectionDefined(LocalWorld world);

    /**
     * Returns true if the region is fully defined for the specified world.
     *
     * @param world the world
     * @return true if a region selection is defined
     */
    public boolean isSelectionDefined(World world);    

    /**
     * @deprecated use {@link #getSelection(World)}
     */
    @Deprecated
    public Region getRegion() throws IncompleteRegionException;

    /**
     * @deprecated use {@link #getSelection(World)}
     */
    @Deprecated
    public Region getSelection(LocalWorld world) throws IncompleteRegionException;

    /**
     * Get the selection region. If you change the region, you should
     * call learnRegionChanges().  If the selection is defined in
     * a different world, the {@code IncompleteRegionException}
     * exception will be thrown.
     *
     * @param world the world
     * @return a region
     * @throws IncompleteRegionException if no region is selected
     */
    public Region getSelection(World world) throws IncompleteRegionException;

    /**
     * Get the selection world.
     *
     * @return the the world of the selection
     */
    public World getSelectionWorld();

    /**
     * Gets the clipboard.
     *
     * @return clipboard
     * @throws EmptyClipboardException thrown if no clipboard is set
     */
    public ClipboardHolder getClipboard() throws EmptyClipboardException;

    /**
     * Sets the clipboard.
     *
     * <p>Pass {@code null} to clear the clipboard.</p>
     *
     * @param clipboard the clipboard, or null if the clipboard is to be cleared
     */
    public void setClipboard(@Nullable ClipboardHolder clipboard);
    
    /**
     * See if tool control is enabled.
     *
     * @return true if enabled
     */
    public boolean isToolControlEnabled();

    /**
     * Change tool control setting.
     *
     * @param toolControl true to enable tool control
     */
    public void setToolControl(boolean toolControl);

    /**
     * Get the maximum number of blocks that can be changed in an edit session.
     *
     * @return block change limit
     */
    public int getBlockChangeLimit();

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param maxBlocksChanged the maximum number of blocks changed
     */
    public void setBlockChangeLimit(int maxBlocksChanged);

    /**
     * Checks whether the super pick axe is enabled.
     *
     * @return status
     */
    public boolean hasSuperPickAxe();

    /**
     * Enable super pick axe.
     */
    public void enableSuperPickAxe();

    /**
     * Disable super pick axe.
     */
    public void disableSuperPickAxe();

    /**
     * Toggle the super pick axe.
     *
     * @return whether the super pick axe is now enabled
     */
    public boolean toggleSuperPickAxe();

    /**
     * Get the position use for commands that take a center point
     * (i.e. //forestgen, etc.).
     *
     * @param player the player
     * @return the position to use
     * @throws IncompleteRegionException thrown if a region is not fully selected
     */
    public Vector getPlacementPosition(Player player) throws IncompleteRegionException;

    /**
     * Toggle placement position.
     *
     * @return whether "place at position 1" is now enabled
     */
    public boolean togglePlacementPosition();

    /**
     * Get a block bag for a player.
     *
     * @param player the player to get the block bag for
     * @return a block bag
     */
    @Nullable
    public BlockBag getBlockBag(Player player);

    /**
     * Get the snapshot that has been selected.
     *
     * @return the snapshot
     */
    @Nullable
    public Snapshot getSnapshot();

    /**
     * Select a snapshot.
     *
     * @param snapshot a snapshot
     */
    public void setSnapshot(@Nullable Snapshot snapshot);

    /**
     * Get the assigned block tool.
     *
     * @return the super pickaxe tool mode
     */
    public BlockTool getSuperPickaxe();

    /**
     * Set the super pick axe tool.
     *
     * @param tool the tool to set
     */
    public void setSuperPickaxe(BlockTool tool);

    /**
     * Get the tool assigned to the item.
     *
     * @param item the item type ID
     * @return the tool, which may be {@link null}
     */
    @Nullable
    public Tool getTool(int item);

    /**
     * Get the brush tool assigned to the item. If there is no tool assigned
     * or the tool is not assigned, the slot will be replaced with the
     * brush tool.
     *
     * @param item the item type ID
     * @return the tool, or {@code null}
     * @throws InvalidToolBindException if the item can't be bound to that item
     */
    public BrushTool getBrushTool(int item) throws InvalidToolBindException;

    /**
     * Set the tool.
     *
     * @param item the item type ID
     * @param tool the tool to set, which can be {@code null}
     * @throws InvalidToolBindException if the item can't be bound to that item
     */
    public void setTool(int item, @Nullable Tool tool) throws InvalidToolBindException;

    /**
     * Returns whether inventory usage is enabled for this session.
     *
     * @return if inventory is being used
     */
    public boolean isUsingInventory();

    /**
     * Set the state of inventory usage.
     *
     * @param useInventory if inventory is to be used
     */
    public void setUseInventory(boolean useInventory);

    /**
     * Get the last script used.
     *
     * @return the last script's name
     */
    @Nullable
    public String getLastScript();

    /**
     * Set the last script used.
     *
     * @param lastScript the last script's name
     */
    public void setLastScript(@Nullable String lastScript);

    /**
     * Tell the player the WorldEdit version.
     *
     * @param player the player
     */
    public void tellVersion(Actor player);

    /**
     * Dispatch a CUI event but only if the actor has CUI support.
     *
     * @param actor the actor
     * @param event the event
     */
    public void dispatchCUIEvent(Actor actor, CUIEvent event);

    /**
     * Dispatch the initial setup CUI messages.
     *
     * @param actor the actor
     */
    public void dispatchCUISetup(Actor actor);

    /**
     * Send the selection information.
     *
     * @param actor the actor
     */
    public void dispatchCUISelection(Actor actor);

    /**
     * Describe the selection to the CUI actor.
     *
     * @param actor the actor
     */
    public void describeCUI(Actor actor);

    /**
     * Handle a CUI initialization message.
     *
     * @param text the message
     */
    public void handleCUIInitializationMessage(String text);

    /**
     * Gets the status of CUI support.
     *
     * @return true if CUI is enabled
     */
    public boolean hasCUISupport();

    /**
     * Sets the status of CUI support.
     *
     * @param support true if CUI is enabled
     */
    public void setCUISupport(boolean support);

    /**
     * Gets the client's CUI protocol version
     *
     * @return the CUI version
     */
    public int getCUIVersion();

    /**
     * Sets the client's CUI protocol version
     *
     * @param cuiVersion the CUI version
     */
    public void setCUIVersion(int cuiVersion);

    /**
     * Detect date from a user's input.
     *
     * @param input the input to parse
     * @return a date
     */
    @Nullable
    public Calendar detectDate(String input);

    /**
     * @deprecated use {@link #createEditSession(Player)}
     */
    @Deprecated
    public EditSession createEditSession(LocalPlayer player);

    /**
     * Construct a new edit session.
     *
     * @param player the player
     * @return an edit session
     */
    @SuppressWarnings("deprecation")
    public EditSession createEditSession(Player player);

    /**
     * Checks if the session has fast mode enabled.
     *
     * @return true if fast mode is enabled
     */
    public boolean hasFastMode();

    /**
     * Set fast mode.
     *
     * @param fastMode true if fast mode is enabled
     */
    public void setFastMode(boolean fastMode);

    /**
     * Get the mask.
     *
     * @return mask, may be null
     */
    public Mask getMask();

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    public void setMask(Mask mask);

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    @SuppressWarnings("deprecation")
    public void setMask(com.sk89q.worldedit.masks.Mask mask);
}
