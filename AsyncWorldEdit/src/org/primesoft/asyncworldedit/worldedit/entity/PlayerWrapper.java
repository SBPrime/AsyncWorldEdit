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

package org.primesoft.asyncworldedit.worldedit.entity;

import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.util.UUID;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.Pair;
import org.primesoft.asyncworldedit.worldedit.world.AsyncWorld;

/**
 *
 * @author SBPrime
 */
public class PlayerWrapper implements Player {

    /**
     * The parrent class
     */
    private final Player m_parent;

    private final Object m_mutex = new Object();

    private UUID m_uuid = null;
    
    private PlayerEntry m_entry = null;

    private Pair<World, AsyncWorld> m_world = null;

    public PlayerWrapper(Player player) {
        m_parent = player;
    }

    
    private PlayerEntry getEntry() {        
        if (m_entry == null) {
            synchronized (m_mutex) {
                if (m_entry == null) {
                    IPlayerManager pm = AsyncWorldEditMain.getInstance().getPlayerManager();
                    m_entry = pm.getPlayer(m_parent.getName());
                }
            }
        }
        return m_entry;
    }
        
    /**
     * Ge the player UUID
     * @return 
     */
    public UUID getUUID() {
        if (m_uuid == null) {
            synchronized (m_mutex) {
                if (m_uuid == null) {
                    if (m_parent instanceof BukkitPlayer) {
                        m_uuid = ((BukkitPlayer) m_parent).getPlayer().getUniqueId();
                    } else {
                        IPlayerManager pm = AsyncWorldEditMain.getInstance().getPlayerManager();
                        PlayerEntry entry = pm.getPlayer(m_parent.getName());
                        return entry.getUUID();
                    }
                }
            }
        }
        return m_uuid;
    }

    @Override
    public boolean ascendLevel() {
        return m_parent.ascendLevel();
    }

    @Override
    public boolean ascendToCeiling(int clearance) {
        return m_parent.ascendToCeiling(clearance);
    }

    @Override
    public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
        return m_parent.ascendToCeiling(clearance, alwaysGlass);
    }

    @Override
    public boolean ascendUpwards(int distance) {
        return m_parent.ascendUpwards(distance);
    }

    @Override
    public boolean ascendUpwards(int distance, boolean alwaysGlass) {
        return m_parent.ascendUpwards(distance, alwaysGlass);
    }

    @Override
    public boolean canDestroyBedrock() {
        return m_parent.canDestroyBedrock();
    }

    @Override
    public void checkPermission(String string) throws AuthorizationException {
        m_parent.checkPermission(string);
    }
       

    @Override
    public boolean descendLevel() {
        return m_parent.descendLevel();
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        m_parent.dispatchCUIEvent(event);
    }

    @Override
    public void findFreePosition() {
        m_parent.findFreePosition();
    }

    @Override
    public void findFreePosition(WorldVector searchPos) {
        m_parent.findFreePosition(searchPos);
    }

    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        m_parent.floatAt(x, y, z, alwaysGlass);
    }

    @Override
    public WorldVector getBlockIn() {
        return m_parent.getBlockIn();
    }

    @Override
    public WorldVector getBlockOn() {
        return m_parent.getBlockOn();
    }

    @Override
    public WorldVector getBlockTrace(int range) {
        return m_parent.getBlockTrace(range);
    }

    @Override
    public WorldVector getBlockTrace(int range, boolean useLastBlock) {
        return m_parent.getBlockTrace(range, useLastBlock);
    }

    @Override
    public WorldVectorFace getBlockTraceFace(int range, boolean useLastBlock) {
        return m_parent.getBlockTraceFace(range, useLastBlock);
    }

    @Override
    public PlayerDirection getCardinalDirection() {
        return m_parent.getCardinalDirection();
    }

    @Override
    public PlayerDirection getCardinalDirection(int yawOffset) {
        return m_parent.getCardinalDirection(yawOffset);
    }

    @Override
    public String[] getGroups() {
        return m_parent.getGroups();
    }

    @Override
    public int getItemInHand() {
        return m_parent.getItemInHand();
    }

    @Override
    public String getName() {
        return m_parent.getName();
    }

    @Override
    public double getPitch() {
        return m_parent.getPitch();
    }

    @Override
    public WorldVector getPosition() {
        return m_parent.getPosition();
    }

    @Override
    public WorldVector getSolidBlockTrace(int range) {
        return m_parent.getSolidBlockTrace(range);
    }

    @Override
    public World getWorld() {
        World world = m_parent.getWorld();
        
        synchronized (m_mutex) {
            if (m_world == null || m_world.getX1() != world) {
                AsyncWorld aWorld = AsyncWorld.wrap(world, getEntry());
                if (aWorld != null) {
                    m_world = new Pair<World, AsyncWorld>(world, aWorld);
                    world = aWorld;
                } else if (m_world != null) {
                    m_world = null;
                }
            } else if (m_world != null) {
                world = m_world.getX2();
            }
        }

        return world;
    }

    @Override
    public double getYaw() {
        return m_parent.getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        m_parent.giveItem(type, amt);
    }

    @Override
    public boolean hasCreativeMode() {
        return m_parent.hasCreativeMode();
    }

    @Override
    public boolean hasPermission(String perm) {
        return m_parent.hasPermission(perm);
    }

    @Override
    public int hashCode() {
        return m_parent.hashCode();
    }

    @Override
    public boolean isHoldingPickAxe() {
        return m_parent.isHoldingPickAxe();
    }

    @Override
    public boolean isPlayer() {
        return m_parent.isPlayer();
    }

    @Override
    public File openFileOpenDialog(String[] extensions) {
        return m_parent.openFileOpenDialog(extensions);
    }

    @Override
    public File openFileSaveDialog(String[] extensions) {
        return m_parent.openFileSaveDialog(extensions);
    }

    @Override
    public boolean passThroughForwardWall(int range) {
        return m_parent.passThroughForwardWall(range);
    }

    @Override
    public void printDebug(String msg) {
        m_parent.printDebug(msg);
    }

    @Override
    public void printError(String msg) {
        m_parent.printError(msg);
    }

    @Override
    public void print(String msg) {
        m_parent.print(msg);
    }

    @Override
    public void printRaw(String msg) {
        m_parent.printRaw(msg);
    }

    @Override
    public void setOnGround(WorldVector searchPos) {
        m_parent.setOnGround(searchPos);
    }

    @Override
    public void setPosition(Vector pos) {
        m_parent.setPosition(pos);
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        m_parent.setPosition(pos, pitch, yaw);
    }

    @Override
    public BaseBlock getBlockInHand()
            throws WorldEditException {
        return m_parent.getBlockInHand();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return m_parent.getInventoryBlockBag();
    }

    @Override
    public String toString() {
        return m_parent.toString();
    }

    @Override
    public Location getLocation() {
        return m_parent.getLocation();
    }

    @Override
    public BaseEntity getState() {
        return m_parent.getState();
    }

    @Override
    public Extent getExtent() {
        return m_parent.getExtent();
    }

    @Override
    public boolean remove() {
        return m_parent.remove();
    }

    @Override
    public <T> T getFacet(Class<? extends T> type) {
        return m_parent.getFacet(type);
    }

    @Override
    public UUID getUniqueId() {
        return m_parent.getUniqueId();
    }

    @Override
    public SessionKey getSessionKey() {
        return m_parent.getSessionKey();
    }
}
