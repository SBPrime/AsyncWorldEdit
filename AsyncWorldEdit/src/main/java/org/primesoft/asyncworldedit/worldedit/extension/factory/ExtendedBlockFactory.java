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
package org.primesoft.asyncworldedit.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import java.util.Set;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.configuration.IWorldEditConfig;
import org.primesoft.asyncworldedit.api.inner.configuration.IConfigBlackList;
import org.primesoft.asyncworldedit.api.inner.configuration.IPremiumWorldEditConfig;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.platform.api.IMaterial;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.worldedit.entity.NoPermsPlayer;
import org.primesoft.asyncworldedit.worldedit.extension.platform.NoPermsActor;

/**
 *
 * @author SBPrime
 */
public class ExtendedBlockFactory extends BlockFactory {

    /**
     * The player manager
     */
    private final IPlayerManager m_playerManager;

    /**
     * The
     */
    private final IPlatform m_platform;

    public ExtendedBlockFactory(IPlatform platform, WorldEdit worldEdit, IPlayerManager playerManager) {
        super(worldEdit);

        m_playerManager = playerManager;
        m_platform = platform;
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
        Actor actor = context.requireActor();
        if (actor == null || !actor.isPlayer() || actor.hasPermission("worldedit.anyblock")) {
            return super.parseFromInput(input, context);
        }
        IPlayerEntry playerEntry = m_playerManager.getPlayer(actor.getUniqueId());

        IPermissionGroup permGroup = playerEntry != null ? playerEntry.getPermissionGroup() : null;
        IWorldEditConfig weConfig = permGroup != null ? permGroup.getWorldEditConfig() : null;
        Set<Integer> disallowedBlocks = weConfig != null ? weConfig.getDisallowedBlocks() : null;

        IPremiumWorldEditConfig premiumWeConfig = weConfig instanceof IPremiumWorldEditConfig ? (IPremiumWorldEditConfig) weConfig : null;
        IConfigBlackList blackListOptions = premiumWeConfig != null ? premiumWeConfig.getBlockListOptions() : null;
        boolean patternEnabled = blackListOptions == null || blackListOptions.isPetternEnabled();

        if (disallowedBlocks == null) {
            return super.parseFromInput(input, patternEnabled ? context : cloneContext(context));
        }

        ParserContext newContext = cloneContext(context);
        BaseBlock resul = super.parseFromInput(input, newContext);

        if (resul == null) {
            return null;
        }

        if (patternEnabled) {
            checkBlocks(disallowedBlocks, resul);
        }

        return resul;
    }

    @Override
    public Set<BaseBlock> parseFromListInput(String input, ParserContext context) throws InputParseException {
        Actor actor = context.requireActor();
        if (actor == null || !actor.isPlayer() || actor.hasPermission("worldedit.anyblock")) {
            return super.parseFromListInput(input, context);
        }
        IPlayerEntry playerEntry = m_playerManager.getPlayer(actor.getUniqueId());

        IPermissionGroup permGroup = playerEntry != null ? playerEntry.getPermissionGroup() : null;
        IWorldEditConfig weConfig = permGroup != null ? permGroup.getWorldEditConfig() : null;
        Set<Integer> disallowedBlocks = weConfig != null ? weConfig.getDisallowedBlocks() : null;

        IPremiumWorldEditConfig premiumWeConfig = weConfig instanceof IPremiumWorldEditConfig ? (IPremiumWorldEditConfig) weConfig : null;
        IConfigBlackList blackListOptions = premiumWeConfig != null ? premiumWeConfig.getBlockListOptions() : null;
        boolean patternEnabled = blackListOptions == null || blackListOptions.isPetternEnabled();

        if (disallowedBlocks == null) {
            return super.parseFromListInput(input, patternEnabled ? context : cloneContext(context));
        }

        ParserContext newContext = cloneContext(context);
        Set<BaseBlock> result = super.parseFromListInput(input, newContext);

        if (result == null) {
            return null;
        }

        if (patternEnabled) {
            for (BaseBlock block : result) {
                checkBlocks(disallowedBlocks, block);
            }
        }

        return result;
    }

    /**
     * Check if block is allowed
     *
     * @param disallowedBlocks
     * @param block
     */
    private void checkBlocks(Set<Integer> disallowedBlocks, BaseBlock block)
            throws InputParseException {
        if (disallowedBlocks == null || block == null) {
            return;
        }

        int id = block.getId();
        if (!disallowedBlocks.contains(id)) {
            return;
        }

        String s;

        IMaterial material = m_platform.getMaterialLibrary().getMaterial(id);
        if (material != null) {
            s = material.getName();
        } else {
            s = String.format("Material_#%1$s", id);
        }

        throw new DisallowedUsageException(String.format("You are not allowed to use '%1$s'", s));
    }

    /**
     * Clone the context
     *
     * @param context
     * @return
     */
    private ParserContext cloneContext(ParserContext context) {
        ParserContext result = new ParserContext(context);

        final Actor actor = result.getActor();
        if (actor == null) {
            return result;
        }

        if (actor instanceof Player) {
            result.setActor(new NoPermsPlayer((Player) actor));
        } else {
            result.setActor(new NoPermsActor(actor));
        }

        return result;
    }
}
