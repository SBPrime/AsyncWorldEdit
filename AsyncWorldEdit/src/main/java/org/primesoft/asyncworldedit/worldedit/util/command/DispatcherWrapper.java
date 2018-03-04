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
package org.primesoft.asyncworldedit.worldedit.util.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.SimpleCommandMapping;
import com.sk89q.worldedit.util.command.SimpleDescription;
import com.sk89q.worldedit.util.command.SimpleDispatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.entity.PlayerWrapper;

/**
 *
 * @author SBPrime
 */
public class DispatcherWrapper implements Dispatcher {

    private final Dispatcher m_parent;

    private final Map<String, CommandMapping> m_commandsOverride = new HashMap<String, CommandMapping>();
    private Set<CommandMapping> m_commandsUnion = null;
    private Set<String> m_aliasesUnion = null;
    
    private final SimpleDispatcher m_implementation;

    public final Dispatcher getParent() {
        return m_parent;
    }

    public DispatcherWrapper(Dispatcher parent) {
        m_parent = parent;
        
        final DispatcherWrapper _this = this;
        m_implementation = new SimpleDispatcher() {
            @Override
            public Set<CommandMapping> getCommands() {
                return _this.getCommands();
            }

            @Override
            public CommandMapping get(String alias) {
                return _this.get(alias);
            }
        };
    }

    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        m_commandsUnion = null;
        m_aliasesUnion = null;

        String[] newAliases = injectOverride(callable, alias);

        if (newAliases.length == 0) {
            return;
        }

        m_parent.registerCommand(callable, newAliases);
    }

    @Override
    public Set<CommandMapping> getCommands() {
        if (m_commandsOverride.isEmpty()) {
            return m_parent.getCommands();
        }

        if (m_commandsUnion == null) {
            Collection<String> aliases = m_parent.getAliases();

            HashSet<CommandMapping> mappings = new HashSet<CommandMapping>(aliases.size());

            for (String a : aliases) {
                String lower = a.toLowerCase();

                CommandMapping m = m_commandsOverride.get(lower);
                if (m == null) {
                    m = m_parent.get(lower);
                }

                if (m != null) {
                    mappings.add(m);
                }
            }

            m_commandsUnion = mappings;
        }

        return Collections.unmodifiableSet(m_commandsUnion);
    }

    
    @Override
    public Set<String> getPrimaryAliases() {
        return m_implementation.getPrimaryAliases();
    }

    @Override
    public Set<String> getAliases() {
        if (m_commandsOverride.isEmpty()) {
            return (Set<String>)m_parent.getAliases();
        }

        if (m_aliasesUnion == null) {
            HashSet<String> tmp = new HashSet<String>();

            for (CommandMapping mapping : getCommands()) {
                String[] aliases = mapping.getAllAliases();
                for (String a : aliases) {
                    if (!tmp.contains(a)) {
                        tmp.add(a);
                    }
                }
            }

            m_aliasesUnion = tmp;
        }

        return Collections.unmodifiableSet(m_aliasesUnion);
    }

    @Override
    public CommandMapping get(String alias) {
        CommandMapping result = m_commandsOverride.get(alias.toLowerCase());
        if (result != null) {
            return result;
        }
        
        return m_parent.get(alias);
    }

    @Override
    public boolean contains(String alias) {
        return m_commandsOverride.containsKey(alias.toLowerCase()) || m_parent.contains(alias);
    }

    @Override
    public Object call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        Map<Object, Object> valuesMap = Reflection.get(locals, Map.class, "locals", "Unable to get locals, player not injected.");
        Map.Entry<Object, Object>[] values = valuesMap.entrySet().toArray(new Map.Entry[0]);
        if (values != null) {
            for (Map.Entry<Object, Object> entry : values) {
                Object key = entry.getKey();
                Object v = entry.getValue();

                if (v instanceof Player) {
                    valuesMap.remove(key);
                    valuesMap.put(key, new PlayerWrapper((Player) v));
                }
            }
        }
        
        return m_implementation.call(arguments, locals, parentCommands);
    }

    @Override
    public SimpleDescription getDescription() {
        return (SimpleDescription)m_parent.getDescription();
    }

    @Override
    public boolean testPermission(CommandLocals locals) {        
        return m_implementation.testPermission(locals);
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return m_implementation.getSuggestions(arguments, locals);
    }

    /**
     * Cleanup commands to allow
     *
     * @param alias
     * @param callable
     * @return unique aliases
     */
    private String[] injectOverride(CommandCallable callable, String[] alias) {
        HashSet<String> known = new HashSet<String>(m_parent.getAliases());
        List<String> result = new ArrayList<String>(alias.length);
        List<String> toOverride = new ArrayList<String>(alias.length);

        for (String a : alias) {
            String lower = a.toLowerCase();
            if (known.contains(lower)) {
                toOverride.add(lower);
            } else {
                result.add(a);
            }
        }

        String[] overrideAlias = toOverride.toArray(new String[0]);
        CommandMapping mapping = new SimpleCommandMapping(callable, overrideAlias);
        for (String a : toOverride) {
            m_commandsOverride.put(a, mapping);
        }

        return result.toArray(new String[0]);
    }
}
