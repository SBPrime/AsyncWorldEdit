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

package org.primesoft.asyncworldedit.worldedit.utils.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.command.Dispatcher;
import java.util.Collection;
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
    
    public final Dispatcher getParent(){
        return m_parent;
    }
    
    public DispatcherWrapper(Dispatcher parent){
        m_parent = parent;
    }
    
    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        m_parent.registerCommand(callable, alias);
    }

    @Override
    public Set<CommandMapping> getCommands() {
        return m_parent.getCommands();
    }

    @Override
    public Collection<String> getPrimaryAliases() {
        return m_parent.getPrimaryAliases();
    }

     @Override
    public Collection<String> getAliases() {
        return m_parent.getAliases();
    }

    @Override
    public CommandMapping get(String alias) {
        return m_parent.get(alias);
    }

    @Override
    public boolean contains(String alias) {
        return m_parent.contains(alias);
    }

    @Override
    public boolean call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {        
        Map<Object, Object> valuesMap = Reflection.get(locals, Map.class, "locals", "Unable to get locals, player not injected.");
        Map.Entry<Object, Object>[] values = valuesMap.entrySet().toArray(new Map.Entry[0]);
        if (values != null) {
            for (Map.Entry<Object, Object> entry : values) {
                Object key = entry.getKey();
                Object v = entry.getValue();
                
                if (v instanceof Player){
                    valuesMap.remove(key);
                    valuesMap.put(key, new PlayerWrapper((Player)v));
                }
            }
        }
        
        return m_parent.call(arguments, locals, parentCommands);
    }

    @Override
    public Description getDescription() {
        return m_parent.getDescription();
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return m_parent.testPermission(locals);
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return m_parent.getSuggestions(arguments, locals);
    }
    
}
