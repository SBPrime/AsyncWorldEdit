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
package org.primesoft.asyncworldedit.worldedit.entity;

import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.extension.platform.NoPermsActor;

/**
 * Actor that does not check perms
 *
 * @author SBPrime
 */
public class NoPermsPlayer extends NoPermsActor implements Player {
    private final Player m_parrent;
    
    public NoPermsPlayer(Player parrent) {
        super(parrent);
        
        m_parrent = parrent;
    }

    

    @Override
    public World getWorld() {
        return m_parrent.getWorld();
    }

    @Override
    public boolean isHoldingPickAxe() {
        return m_parrent.isHoldingPickAxe();
    }

    @Override
    public PlayerDirection getCardinalDirection(int yawOffset) {
        return m_parrent.getCardinalDirection(yawOffset);
    }

    @Override
    public int getItemInHand() {
        return m_parrent.getItemInHand();
    }

    @Override
    public BaseBlock getBlockInHand() throws WorldEditException {
        return m_parrent.getBlockInHand();
    }

    @Override
    public void giveItem(int type, int amount) {
        m_parrent.giveItem(type, amount);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return m_parrent.getInventoryBlockBag();
    }

    @Override
    public boolean hasCreativeMode() {
        return m_parrent.hasCreativeMode();
    }

    @Override
    public void findFreePosition(WorldVector searchPos) {
        m_parrent.findFreePosition(searchPos);
    }

    @Override
    public void setOnGround(WorldVector searchPos) {
        m_parrent.setOnGround(searchPos);
    }

    @Override
    public void findFreePosition() {
        m_parrent.findFreePosition();
    }

    @Override
    public boolean ascendLevel() {
        return m_parrent.ascendLevel();
    }

    @Override
    public boolean descendLevel() {
        return m_parrent.descendLevel();
    }

    @Override
    public boolean ascendToCeiling(int clearance) {
        return m_parrent.ascendToCeiling(clearance);
    }

    @Override
    public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
        return m_parrent.ascendToCeiling(clearance, alwaysGlass);
    }

    @Override
    public boolean ascendUpwards(int distance) {
        return m_parrent.ascendUpwards(distance);
    }

    @Override
    public boolean ascendUpwards(int distance, boolean alwaysGlass) {
        return m_parrent.ascendUpwards(distance, alwaysGlass);
    }

    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        m_parrent.floatAt(x, y, z, alwaysGlass);
    }

    @Override
    public WorldVector getBlockIn() {
        return m_parrent.getBlockIn();
    }

    @Override
    public WorldVector getBlockOn() {
        return m_parrent.getBlockOn();
    }

    @Override
    public WorldVector getBlockTrace(int range, boolean useLastBlock) {
        return m_parrent.getBlockTrace(range, useLastBlock);
    }

    @Override
    public WorldVectorFace getBlockTraceFace(int range, boolean useLastBlock) {
        return m_parrent.getBlockTraceFace(range, useLastBlock);
    }

    @Override
    public WorldVector getBlockTrace(int range) {
        return m_parrent.getBlockTrace(range);
    }

    @Override
    public WorldVector getSolidBlockTrace(int range) {
        return m_parrent.getSolidBlockTrace(range);
    }

    @Override
    public PlayerDirection getCardinalDirection() {
        return m_parrent.getCardinalDirection();
    }

    @Override
    public WorldVector getPosition() {
        return m_parrent.getPosition();
    }

    @Override
    public double getPitch() {
        return m_parrent.getPitch();
    }

    @Override
    public double getYaw() {
        return m_parrent.getYaw();
    }

    @Override
    public boolean passThroughForwardWall(int range) {
        return m_parrent.passThroughForwardWall(range);
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        m_parrent.setPosition(pos, pitch, yaw);
    }

    @Override
    public void setPosition(Vector pos) {
        m_parrent.setPosition(pos);
    }

    @Override
    public BaseEntity getState() {
        return m_parrent.getState();
    }

    @Override
    public Location getLocation() {
        return m_parrent.getLocation();
    }

    @Override
    public Extent getExtent() {
        return m_parrent.getExtent();
    }

    @Override
    public boolean remove() {
        return m_parrent.remove();
    }

    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return m_parrent.getFacet(cls);
    }
}
