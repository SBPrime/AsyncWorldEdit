/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.lang.reflect.Method;

/**
 *
 * @author SBPrime
 */
public class CommandsWrapper extends CommandsManager<LocalPlayer> {

    private final CommandsManager<LocalPlayer> m_parent;
    private final WorldEditPlugin m_wePlugin;
    private final WorldEdit m_worldEdit;

    public CommandsWrapper(WorldEdit worldEdit, WorldEditPlugin wePlugin,
            CommandsManager<LocalPlayer> parent) {
        m_parent = parent;
        m_wePlugin = wePlugin;
        m_worldEdit = worldEdit;
    }

    @Override
    public boolean hasPermission(LocalPlayer t, String string) {
        return m_parent.hasPermission(t, string);
    }

    @Override
    protected void checkPermission(LocalPlayer player, Method method) throws CommandException {
        if (!player.isPlayer() && !method.isAnnotationPresent(Console.class)) {
            throw new UnhandledCommandException();
        }

        super.checkPermission(player, method);
    }

    @Override
    public void invokeMethod(Method parent, String[] args, LocalPlayer player,
            Method method, Object instance, Object[] methodArgs, int level) throws CommandException {

        LocalPlayer newPlayer;

        if (player != null && (player instanceof BukkitPlayer)) {
            newPlayer = new BukkitPlayerWrapper(m_wePlugin, m_worldEdit.getServer(),
                    (BukkitPlayer) player);
        } else {
            newPlayer = player;
        }

        if (methodArgs != null) {
            for (int i = 0; i < methodArgs.length; i++) {
                Object arg = methodArgs[i];
                if (arg instanceof BukkitPlayer) {
                    if (arg == player) {
                        methodArgs[i] = newPlayer;
                    } else {
                        methodArgs[i] = new BukkitPlayerWrapper(m_wePlugin, m_worldEdit.getServer(), (BukkitPlayer) arg);
                    }
                }
            }
        }        

        m_parent.invokeMethod(parent, args, newPlayer, method, instance, methodArgs, level);
    }

}
