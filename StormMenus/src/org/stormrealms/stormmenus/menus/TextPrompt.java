package org.stormrealms.stormmenus.menus;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.stormrealms.stormspigot.Promise;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.var;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TextPrompt {
    @Getter private final String title;
    @Getter private final String defaultInput;
    @Getter private final Promise<String> promise;

    public static Promise<String> show(String title, String defaultInput, Player player) {
        var promise = new Promise<String>();

        var textPrompt = new TextPrompt(title, defaultInput, promise);


        return promise;
    }
}