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
package org.primesoft.asyncworldedit.worldedit;

import java.util.Objects;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionBuilder;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.World;
import org.primesoft.asyncworldedit.api.IWorldeditIntegrator;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionBuilder;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSessionFactory extends EditSessionFactory implements IAsyncEditSessionFactory {
    private final IAsyncWorldEditCore m_aweCore;
    private final IPlayerManager m_playerManager;

    /**
     * Get the player UUID
     *
     */
    private IPlayerEntry getIPlayerEntry(Actor player) {
        if (player == null) {
            return m_playerManager.getUnknownPlayer();
        }
        return m_playerManager.getPlayer(player.getUniqueId());
    }

    private Actor getActor(IPlayerEntry playerEntry) {
        if (playerEntry == null) {
            return null;
        }

        IWorldeditIntegrator weIntegrator = m_aweCore.getWorldEditIntegrator();
        if (weIntegrator == null) {
            return null;
        }

        return weIntegrator.wrapActor(playerEntry);
    }

    public AsyncEditSessionFactory(IAsyncWorldEditCore aweCore) {
        m_aweCore = aweCore;
        m_playerManager = aweCore.getPlayerManager();
    }

    @Override
    public EditSession getEditSession(final World world, final int maxBlocks) {
        return getEditSession(world, maxBlocks, null, (Actor)null);
    }

    @Override
    public EditSession getEditSession(final World world, final int maxBlocks, final Actor actor) {
        return getEditSession(world, maxBlocks, null, actor);
    }

    @Override
    public EditSession getEditSession(final World world, final int maxBlocks, final BlockBag blockBag) {
        return getEditSession(world, maxBlocks, blockBag, (Actor)null);
    }

    @Override
    public EditSession getEditSession(final World world, final int maxBlocks, final BlockBag blockBag, final Actor actor) {
        return getEditSession(world, maxBlocks, blockBag, actor, getIPlayerEntry(actor));
    }

    @Override
    public EditSession getEditSession(final World world, final int maxBlocks, final BlockBag blockBag, final IPlayerEntry playerEntry) {
        return getEditSession(world, maxBlocks, blockBag, getActor(playerEntry), playerEntry);
    }

    private EditSession getEditSession(
            final World world,
            final int maxBlocks,
            final BlockBag blockBag,
            final Actor actor,
            final IPlayerEntry playerEntry) {
        return get(false, world, maxBlocks, blockBag, actor, playerEntry);
    }

    @Override
    public IThreadSafeEditSession getThreadSafeEditSession(final World world, final int maxBlocks) {
        return getThreadSafeEditSession(world, maxBlocks, null, (Actor)null);
    }

    @Override
    public IThreadSafeEditSession getThreadSafeEditSession(final World world, final int maxBlocks, final Actor actor) {
        return getThreadSafeEditSession(world, maxBlocks, null, actor);
    }

    @Override
    public IThreadSafeEditSession getThreadSafeEditSession(final World world, final int maxBlocks, final BlockBag blockBag) {
        return getThreadSafeEditSession(world, maxBlocks, blockBag, (Actor)null);
    }

    @Override
    public IThreadSafeEditSession getThreadSafeEditSession(final World world, final int maxBlocks, final BlockBag blockBag, final Actor actor) {
        return getThreadSafeEditSession(world, maxBlocks, blockBag, actor, getIPlayerEntry(actor));
    }

    @Override
    public IThreadSafeEditSession getThreadSafeEditSession(final World world, final int maxBlocks, final BlockBag blockBag, final IPlayerEntry playerEntry) {
        return getThreadSafeEditSession(world, maxBlocks, blockBag, getActor(playerEntry), playerEntry);
    }

    private IThreadSafeEditSession getThreadSafeEditSession(
            final World world,
            final int maxBlocks,
            final BlockBag blockBag,
            final Actor actor,
            final IPlayerEntry playerEntry) {

        return (IThreadSafeEditSession) get(true, world, maxBlocks, blockBag, actor, playerEntry);
    }


    private EditSession get(
            final boolean threadSafe,
            final World world,
            final int maxBlocks,
            final BlockBag blockBag,
            final Actor actor,
            final IPlayerEntry playerEntry) {

        final EditSessionBuilder builder = WorldEdit.getInstance().newEditSessionBuilder();
        final IAsyncEditSessionBuilder aweBuilder = (IAsyncEditSessionBuilder)(Object)builder;

        builder.world(world)
                .maxBlocks(maxBlocks)
                .blockBag(blockBag)
                .actor(actor)
                .build();
        aweBuilder.setPlayerEntry(playerEntry);
        if (threadSafe) {
            aweBuilder.theadSafeEditSession();
        }

        final EditSession result = builder.build();

        if (playerEntry != null && !Objects.equals(playerEntry, m_playerManager.getUnknownPlayer())) {
            m_aweCore.getPlotMeFix().setMask(playerEntry.getUUID());
        }

        return result;
    }
}
