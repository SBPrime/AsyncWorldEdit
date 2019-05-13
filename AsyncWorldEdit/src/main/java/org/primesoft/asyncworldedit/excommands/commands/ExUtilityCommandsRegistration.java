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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.Command.Condition;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.CommandParts;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistration;

/**
 *
 * @author SBPrime
 */
public class ExUtilityCommandsRegistration extends BaseCommandsRegistration {

    private final CommandArgument m_partPattern;
    private final CommandArgument m_partRadius;
    private final CommandArgument m_partDepth;

    private FillCommands m_fillCommands;
    private final IAsyncWorldEditCore m_aweCore;

    public ExUtilityCommandsRegistration(IAsyncWorldEditCore aweCore) {
        m_aweCore = aweCore;
        
        m_partPattern = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The blocks to fill with")).defaultsTo(ImmutableList.of()).ofTypes(ImmutableList.of(KEY_PATTERN)).build();
        m_partRadius = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to fill in")).defaultsTo(ImmutableList.of()).ofTypes(ImmutableList.of(KEY_DOUBLE)).build();
        m_partDepth = CommandParts.arg(TranslatableComponent.of("depth"), TextComponent.of("The depth to fill")).defaultsTo(ImmutableList.of("1")).ofTypes(ImmutableList.of(KEY_INTEGER)).build();
    }

    @Override
    public void build(ICommandsRegistration cr) {
        super.build(cr);
        
        final FillCommand[] fillCommands = new FillCommand[]{
            new FillCommand("/fillxz", new String[]{"/fillzx"}, "fillxz", true, false, true),
            new FillCommand("/fillxy", new String[]{"/fillyx"}, "fillxy", true, true, false),
            new FillCommand("/fillyz", new String[]{"/fillzy"}, "fillyz", false, true, true),
            new FillCommand("/fill3d", new String[]{"/fillxyz", "/fillxzy", "/fillyxz", "/fillyzx", "/fillzxy", "/fillzyx"}, "fill3d", true, true, true)
        };

        final Class[] executorParams = new Class[]{Player.class, LocalSession.class, EditSession.class, Pattern.class, Double.TYPE, Integer.TYPE};
        for (FillCommand entry : fillCommands) {
            register(entry.name, entry.aliases, "Fill a hole",
                    new CommandPart[]{m_partPattern, m_partRadius, m_partDepth},
                    FillCommands.class, entry.method, executorParams,
                    parameters -> execFillCommand(parameters, entry)
            );
        }
    }

    private int execFillCommand(CommandParameters parameters, FillCommand entry) throws WorldEditException {
        if (m_fillCommands == null) {
            m_fillCommands = new FillCommands(m_aweCore.getWorldEditIntegrator().getWE(), m_aweCore);
        }
        
        return m_fillCommands.fill(
                player(parameters), session(parameters),
                editSession(parameters), pattern(parameters),
                radius(parameters), depth(parameters),
                entry.axisX, entry.axisY, entry.axisZ);
    }

    private void register(String name, String[] aliases, String description,
            CommandPart[] params,
            Class<?> executorCls, String executorMethod, Class[] executorParams,
            WorldEditFunction toExec) {
        m_commandManager.register(name, b -> {
            b.aliases(Collections.unmodifiableCollection(Stream.of(aliases).collect(Collectors.toList())));
            b.description(TextComponent.of(description));
            b.parts(Collections.unmodifiableCollection(Stream.of(params).collect(Collectors.toList())));

            final Method commandMethod = RegistrationUtil.getCommandMethod(
                    executorCls, executorMethod,
                    executorParams);

            b.action(parameters -> executeMethod(parameters, commandMethod, toExec));
            Condition condition = m_commandPermissionsConditionGenerator.generateCondition(commandMethod);

            b.condition(condition);
        });
    }

    private int executeMethod(CommandParameters parameters, Method cmdMethod,
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

    private Pattern pattern(CommandParameters parameters) {
        return (Pattern) m_partPattern.value(parameters).asSingle(KEY_PATTERN);
    }

    private double radius(CommandParameters parameters) {
        return (Double) m_partRadius.value(parameters).asSingle(KEY_DOUBLE);
    }

    private int depth(CommandParameters parameters) {
        return (Integer) m_partDepth.value(parameters).asSingle(KEY_INTEGER);
    }

    private static class FillCommand {

        public final String name;
        public final String[] aliases;
        public final String method;
        public final boolean axisX;
        public final boolean axisY;
        public final boolean axisZ;

        public FillCommand(String name, String[] aliases,
                String method,
                boolean axisX, boolean axisY, boolean axisZ) {
            this.name = name;
            this.aliases = aliases;
            this.method = method;
            this.axisX = axisX;
            this.axisY = axisY;
            this.axisZ = axisZ;
        }
    }

    @FunctionalInterface
    private interface WorldEditFunction {

        int exec(CommandParameters parameters) throws WorldEditException;
    }
}
