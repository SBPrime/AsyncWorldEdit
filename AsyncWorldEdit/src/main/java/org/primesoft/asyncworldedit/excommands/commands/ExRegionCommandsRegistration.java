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
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.Command.Condition;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistration;

/**
 *
 * @author SBPrime
 */
public class ExRegionCommandsRegistration extends BaseCommandsRegistration {

    private RegionCommands m_regionCommands;

    private final CommandArgument m_partFrom;
    private final CommandArgument m_partTo;
    private final CommandArgument m_partCountMove;
    private final CommandArgument m_partCountStack;

    private final CommandArgument m_partDirectionMove;
    private final CommandArgument m_partDirectionStack;
    private final CommandArgument m_partRaplace;
    private final NoArgCommandFlag m_partMoveSelectionStack;
    private final NoArgCommandFlag m_partIgnoreAirBlocks;
    private final CommandArgument m_partMask;
    private final NoArgCommandFlag m_partMoveSelectionMove;

    public ExRegionCommandsRegistration() {
        m_partFrom = CommandParts.arg(TranslatableComponent.of("from"), TextComponent.of("The mask representing blocks to replace")).defaultsTo(Collections.singletonList("")).ofTypes(Collections.singletonList(KEY_MASK)).build();
        m_partTo = CommandParts.arg(TranslatableComponent.of("to"), TextComponent.of("The pattern of blocks to replace with")).defaultsTo(ImmutableList.of()).ofTypes(Collections.singletonList(KEY_PATTERN)).build();

        m_partCountMove = CommandParts.arg(TranslatableComponent.of("count"), TextComponent.of("# of blocks to move")).defaultsTo(Collections.singletonList("1")).ofTypes(Collections.singletonList(KEY_INTEGER)).build();
        m_partCountStack = CommandParts.arg(TranslatableComponent.of("count"), TextComponent.of("# of copies to stack")).defaultsTo(Collections.singletonList("1")).ofTypes(Collections.singletonList(KEY_INTEGER)).build();

        m_partDirectionMove = CommandParts.arg(TranslatableComponent.of("direction"), TextComponent.of("The direction to move")).defaultsTo(Collections.singletonList("me")).ofTypes(Collections.singletonList(KEY_BLOCK_VECTOR3_DIAGONALS)).build();
        m_partDirectionStack = CommandParts.arg(TranslatableComponent.of("direction"), TextComponent.of("The direction to stack")).defaultsTo(Collections.singletonList("me")).ofTypes(Collections.singletonList(KEY_BLOCK_VECTOR3_DIAGONALS)).build();

        m_partRaplace = CommandParts.arg(TranslatableComponent.of("replace"), TextComponent.of("The pattern of blocks to leave")).defaultsTo(Collections.singletonList("air")).ofTypes(Collections.singletonList(KEY_PATTERN)).build();

        m_partMoveSelectionMove = CommandParts.flag('s', TextComponent.of("Shift the selection to the target location")).build();
        m_partMoveSelectionStack = CommandParts.flag('s', TextComponent.of("Shift the selection to the last stacked copy")).build();

        m_partIgnoreAirBlocks = CommandParts.flag('a', TextComponent.of("Ignore air blocks")).build();

        m_partMask = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("Sets a source mask so that excluded blocks become air")).defaultsTo(ImmutableList.of()).ofTypes(Collections.singletonList(KEY_MASK)).build();
    }

    @Override
    public final void build(ICommandsRegistration cr) {
        super.build(cr);

        m_commandManager.register("/replacend", b -> {
            b.aliases(Collections.unmodifiableList(Stream.of("/rend", "/repnd").collect(Collectors.toList())));
            b.description(TextComponent.of("Replace all blocks in the selection with another and keep the data"));
            b.parts(ImmutableList.of(m_partFrom, m_partTo));

            Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "replacend", new Class[]{Player.class, EditSession.class, Region.class, Mask.class, Pattern.class});
            b.action(parameters -> executeMethod(parameters, commandMethod, this::doReplaceNd));

            Condition condition = m_commandPermissionsConditionGenerator.generateCondition(commandMethod);
            b.condition(condition);
        });

        m_commandManager.register("/mmove", (b) -> {
            b.aliases(ImmutableList.of("/maskmove", "/maskedmove", "/movem", "/movemasked"));
            b.description(TextComponent.of("Move the contents of the selection"));
            b.parts(ImmutableList.of(m_partMask, m_partCountMove, m_partDirectionMove, m_partRaplace, m_partMoveSelectionMove, m_partIgnoreAirBlocks));

            Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "move",
                    new Class[]{Player.class, EditSession.class, LocalSession.class, Region.class, Integer.TYPE,
                        BlockVector3.class, Pattern.class, Boolean.TYPE, Boolean.TYPE, Mask.class});

            b.action(parameters -> executeMethod(parameters, commandMethod, this::doMove));

            Condition condition = m_commandPermissionsConditionGenerator.generateCondition(commandMethod);
            b.condition(condition);
        });
    

        m_commandManager.register("/mstack", b -> {
            b.aliases(ImmutableList.of("/maskstack", "/maskedstack", "/stackm", "/stackmasked"));
            b.description(TextComponent.of("Repeat the contents of the selection"));
            b.parts(ImmutableList.of(m_partMask, m_partCountStack, m_partDirectionStack, m_partMoveSelectionStack, m_partIgnoreAirBlocks));

            Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "stack",
                    new Class[]{Player.class, EditSession.class, LocalSession.class, Region.class, Integer.TYPE,
                        BlockVector3.class, Boolean.TYPE, Boolean.TYPE, Mask.class});
            b.action(parameters -> executeMethod(parameters, commandMethod, this::doStack));

            Condition condition = m_commandPermissionsConditionGenerator.generateCondition(commandMethod);
            b.condition(condition);
        });
    }

    private RegionCommands getRegionCommands() {
        if (m_regionCommands == null) {
            m_regionCommands = new RegionCommands();
        }

        return m_regionCommands;
    }

    private int doReplaceNd(CommandParameters parameters) throws WorldEditException {
        return getRegionCommands().replacend(
                player(parameters), editSession(parameters), region(parameters),
                from(parameters), to(parameters)
        );
    }

    private int doMove(CommandParameters parameters) throws WorldEditException {
        return getRegionCommands().move(
                player(parameters), editSession(parameters), session(parameters),
                region(parameters), countMove(parameters), directionMove(parameters),
                replace(parameters), moveSelectionMove(parameters), ignoreAirBlocks(parameters), 
                mask(parameters)
        );
    }

    private int doStack(CommandParameters parameters) throws WorldEditException {
        return getRegionCommands().stack(
                player(parameters), editSession(parameters), session(parameters),
                region(parameters), countStack(parameters), directionStack(parameters),
                moveSelectionStack(parameters), ignoreAirBlocks(parameters),
                mask(parameters)
        );
    }

    private Mask from(CommandParameters parameters) {
        return (Mask) m_partFrom.value(parameters).asSingle(KEY_MASK);
    }

    private Pattern to(CommandParameters parameters) {
        return (Pattern) m_partTo.value(parameters).asSingle(KEY_PATTERN);
    }

    private Pattern replace(CommandParameters parameters) {
        return (Pattern) m_partRaplace.value(parameters).asSingle(KEY_PATTERN);
    }

    private boolean ignoreAirBlocks(CommandParameters parameters) {
        return m_partIgnoreAirBlocks.in(parameters);
    }

    private boolean moveSelectionMove(CommandParameters parameters) {
        return m_partMoveSelectionMove.in(parameters);
    }

    private boolean moveSelectionStack(CommandParameters parameters) {
        return m_partMoveSelectionStack.in(parameters);
    }

    private int countMove(CommandParameters parameters) {
        return (Integer) m_partCountMove.value(parameters).asSingle(KEY_INTEGER);
    }

    private int countStack(CommandParameters parameters) {
        return (Integer) m_partCountStack.value(parameters).asSingle(KEY_INTEGER);
    }

    private BlockVector3 directionMove(CommandParameters parameters) {
        return (BlockVector3) m_partDirectionMove.value(parameters).asSingle(KEY_BLOCK_VECTOR3_DIAGONALS);
    }

    private BlockVector3 directionStack(CommandParameters parameters) {
        return (BlockVector3) m_partDirectionStack.value(parameters).asSingle(KEY_BLOCK_VECTOR3_DIAGONALS);
    }

    private Mask mask(CommandParameters parameters) {
        return (Mask) m_partMask.value(parameters).asSingle(KEY_MASK);
    }
}
