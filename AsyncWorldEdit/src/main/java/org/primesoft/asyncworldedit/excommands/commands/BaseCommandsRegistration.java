/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.excommands.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistration;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistrationDelegate;

/**
 *
 * @author SBPrime
 */
public abstract class BaseCommandsRegistration implements ICommandsRegistrationDelegate {

    protected static final Key KEY_PATTERN = Key.of(Pattern.class);
    protected static final Key KEY_DOUBLE = Key.of(Double.class);
    protected static final Key KEY_INTEGER = Key.of(Integer.class);
    protected static final Key KEY_MASK = Key.of(Mask.class);
    protected static final Key KEY_STRING = Key.of(String.class);
    protected static final Key KEY_PLAYER = Key.of(Player.class);
    protected static final Key KEY_LOCAL_SESSION = Key.of(LocalSession.class);
    protected static final Key KEY_EDIT_SESSION = Key.of(EditSession.class);
    protected static final Key KEY_ACTOR = Key.of(Actor.class);
    protected static final Key KEY_REGION_SELECTION = Key.of(Region.class, Selection.class);
    protected static final Key KEY_BLOCK_VECTOR3_DIAGONALS = Key.of(BlockVector3.class,
            (new Object() {
                Annotation a(@Direction(includeDiagonals = true) Object ah) {
                    return this.getClass().getDeclaredMethods()[0].getParameterAnnotations()[0][0];
                }
            }).a((Object) null)
    );

    protected CommandManager m_commandManager;
    protected CommandPermissionsConditionGenerator m_commandPermissionsConditionGenerator;
    protected List m_listeners;

    @Override
    public void build(ICommandsRegistration cr) {
        m_commandManager = cr.getCommandManager();
        m_listeners = cr.getListeners();

        m_commandPermissionsConditionGenerator = cr.getCommandPermissionsConditionGenerator();
    }

    protected int executeMethod(CommandParameters parameters, Method cmdMethod,
            WorldEditFunction toExec) throws WorldEditException {

        RegistrationUtil.listenersBeforeCall(m_listeners, cmdMethod, parameters);

        try {
            int result = toExec.exec(parameters);
            RegistrationUtil.listenersAfterCall(m_listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable ex) {
            RegistrationUtil.listenersAfterThrow(m_listeners, cmdMethod, parameters, ex);
            throw ex;
        }
    }

    protected final Player player(CommandParameters parameters) {
        return (Player) RegistrationUtil.requireOptional(KEY_PLAYER, "player", parameters.injectedValue(KEY_PLAYER));
    }

    protected final LocalSession session(CommandParameters parameters) {
        return (LocalSession) RegistrationUtil.requireOptional(KEY_LOCAL_SESSION, "session", parameters.injectedValue(KEY_LOCAL_SESSION));
    }

    protected final Actor actor(CommandParameters parameters) {
        return (Actor) RegistrationUtil.requireOptional(KEY_ACTOR, "actor", parameters.injectedValue(KEY_ACTOR));
    }

    protected final EditSession editSession(CommandParameters parameters) {
        return (EditSession) RegistrationUtil.requireOptional(KEY_EDIT_SESSION, "editSession", parameters.injectedValue(KEY_EDIT_SESSION));
    }

    protected final Region region(CommandParameters parameters) {
        return (Region) RegistrationUtil.requireOptional(KEY_REGION_SELECTION, "region", parameters.injectedValue(KEY_REGION_SELECTION));
    }

    @FunctionalInterface
    protected interface WorldEditFunction {

        int exec(CommandParameters parameters) throws WorldEditException;
    }
}
