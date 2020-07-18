package org.stormrealms.stormmenus.listeners;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.menus.TextPrompt;
import org.stormrealms.stormspigot.PacketSubscriptionManager;

import lombok.var;
import net.minecraft.server.Containers;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayOutOpenWindow;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.ChatMessage;

@Component
public class TextPromptListeners {
	@Autowired
	private MenuManager menuManager;

    @PostConstruct
    public void subscribeToPackets() {
        // TODO(Yevano)
        // PacketSubscriptionManager.getInstance()
        //     .subscribe(PacketPlayInCloseWindow.class)
        //     
        //     .then(hook -> {
        //         System.out.println("Intercepting close packet.");
        //         var playerConnection = hook.getPlayerConnection();
        //         var uuid = playerConnection.player.getUniqueID();
        //         
        //         if(menuManager.playerHasPromptOpen(uuid)) {
        //             var textPrompt = menuManager.getPlayerTextPrompt(uuid);
        //             playerConnection.sendPacket(new PacketPlayOutOpenWindow(1, Containers.ANVIL, new ChatMessage(textPrompt.getTitle())));
        //         }
        //     });
    }
}