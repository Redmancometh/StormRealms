package org.stormrealms.stormmenus.listeners;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.menus.TextPrompt;
import org.stormrealms.stormnet.PacketSubscriptionManager;

import net.minecraft.server.Containers;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayOutOpenWindow;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.ChatMessage;

@Component
public class TextPromptListeners {
	/*
	@Autowired
	private MenuManager menuManager;

	@Autowired
	private PacketSubscriptionManager packetSubscriptionManager;

	@PostConstruct
	public void subscribeToPackets() {
		// TODO(Yevano)
		packetSubscriptionManager.subscribe(PacketPlayInCloseWindow.class).then(hook -> {
			System.out.println("Intercepting close packet.");
			PlayerConnection playerConnection = hook.getPlayerConnection();
			UUID uuid = playerConnection.player.getUniqueID();
			if (menuManager.playerHasPromptOpen(uuid)) {
				TextPrompt textPrompt = menuManager.getPlayerTextPrompt(uuid);
				playerConnection.sendPacket(
						new PacketPlayOutOpenWindow(1, Containers.ANVIL, new ChatMessage(textPrompt.getTitle())));
			}
		});
	}*/
}
