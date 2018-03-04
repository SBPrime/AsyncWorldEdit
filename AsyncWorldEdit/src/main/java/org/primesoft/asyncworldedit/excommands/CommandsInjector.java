/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.excommands;

import org.primesoft.asyncworldedit.excommands.commands.ChunkCommands;
import org.primesoft.asyncworldedit.excommands.commands.RegionCommands;
import org.primesoft.asyncworldedit.excommands.commands.SchematicCommands;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.SimpleCommandMapping;
import com.sk89q.worldedit.util.command.SimpleDescription;
import com.sk89q.worldedit.util.command.SimpleDispatcher;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;
import java.lang.reflect.Field;
import java.util.Set;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.excommands.commands.ClipboardCommands;
import org.primesoft.asyncworldedit.excommands.commands.FillCommands;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.util.command.DispatcherWrapper;

/**
 * The AsyncWorldEdit commands injector
 *
 * @author SBPrime
 */
public class CommandsInjector {
    private final static Field s_simpleCommandCallable_callable 
            = Reflection.findField(SimpleCommandMapping.class, "callable", "Unable to get callable field from SimpleCommandMapping");
    
    
    /**
     * Start injecting new AWE commands to WE
     *
     * @param worldEdit
     * @param api
     * @param platform
     * @param commandManager
     */
    public static void injectCommands(WorldEdit worldEdit, IAsyncWorldEdit api,
            Platform platform, CommandManager commandManager) {
        Dispatcher dispatcher = commandManager.getDispatcher();

        if (dispatcher == null) {
            log("Unable to inject commands: No dispatcher found");
            return;
        }

        CommandMapping mainCommand = dispatcher.get("we");
        if (mainCommand == null || !(mainCommand instanceof SimpleCommandMapping)) {
            log("Unable to inject commands: Unable to get main WorldEdit command");
            return;
        }

        ParametricBuilder builder = findBuilder(dispatcher, mainCommand);

        if (builder == null) {
            log("Unable to inject commands: builder not found.");
            return;
        }

        builder.registerMethodsAsCommands(dispatcher, new RegionCommands(worldEdit, api));
        builder.registerMethodsAsCommands(dispatcher, new ClipboardCommands());
        builder.registerMethodsAsCommands(dispatcher, new ScriptingCommands(worldEdit));
        builder.registerMethodsAsCommands(dispatcher, new FillCommands(worldEdit, api));        
        injectCommmands(builder, dispatcher.get("schematic"), new SchematicCommands(worldEdit, api));

        if (api.getDirectChunkAPI() != null) {
            SimpleDispatcher groupChunks = createGroup("Chunk manipulation commands");
            builder.registerMethodsAsCommands(groupChunks, new ChunkCommands(api));
            dispatcher.registerCommand(groupChunks, "chunk");
            log("Direct chunk commands...enabled");
        } else {
            log("Direct chunk commands...disabled");
        }

        //SimpleDispatcher groupTest = createGroup("Test commands");                
        //builder.registerMethodsAsCommands(groupTest, new TestCommands(aweMain.getAPI()));
        //dispatcher.registerCommand(groupTest, "test");
        platform.registerCommands(dispatcher);
    }

    /**
     * Inject a new command to WorldEdit
     *
     * @param builder
     * @param commandGroup
     * @param object
     */
    private static void injectCommmands(ParametricBuilder builder, CommandMapping commandGroup, Object object) {
        if (commandGroup == null) {
            log("Unable to inject commands: command group not found.");
            return;
        }
        if (object == null) {
            return;
        }

        SimpleCommandMapping simpleCommandMapping = (SimpleCommandMapping) commandGroup;
        CommandCallable mainCallable = simpleCommandMapping.getCallable();
        if (mainCallable == null || !(mainCallable instanceof SimpleDispatcher) || s_simpleCommandCallable_callable == null) {
            log("Unable to inject commands: callable not found.");
            return;
        }
        
        Dispatcher dispatcher = (Dispatcher) mainCallable;
        if (!(dispatcher instanceof DispatcherWrapper)) {
            dispatcher = new DispatcherWrapper(dispatcher);
            Reflection.set(simpleCommandMapping, s_simpleCommandCallable_callable, dispatcher, "Unable to inject new command dispatcher.");
        }                
        
        
        builder.registerMethodsAsCommands(dispatcher, object);
    }

    /**
     * Find the WorldEdit ParametricBuilder to allow command injection
     *
     * @param dispatcher
     * @param mainCommand
     * @return
     */
    private static ParametricBuilder findBuilder(Dispatcher dispatcher, CommandMapping mainCommand) {
        CommandCallable callable = null;
        boolean loop = false;
        do {
            callable = mainCommand.getCallable();
            if (callable == null) {
                log("Unable to inject commands: Unable to get command callable.");
                return null;
            }

            loop = callable instanceof SimpleDispatcher;
            if (loop) {
                SimpleDispatcher mainDispatcher = (SimpleDispatcher) callable;
                Set<CommandMapping> subCommands = mainDispatcher.getCommands();
                if (subCommands == null || subCommands.isEmpty()) {
                    log("Unable to inject commands: Unable to get command callable.");
                    return null;
                }
                mainCommand = subCommands.iterator().next();
            }
        } while (loop);

        return Reflection.get(callable.getClass(), ParametricBuilder.class,
                callable, "builder", "Unable to get builder");
    }

    private static SimpleDispatcher createGroup(String description) {
        SimpleDispatcher command = new SimpleDispatcher();
        SimpleDescription cmdDescription = command.getDescription();

        cmdDescription.setDescription(description);

        return command;
    }
}
