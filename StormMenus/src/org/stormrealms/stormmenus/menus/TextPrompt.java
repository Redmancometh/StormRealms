package org.stormrealms.stormmenus.menus;

import java.util.UUID;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormnet.Promise;

import com.sun.istack.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.ChatMessage;
import net.minecraft.server.Containers;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.PacketPlayOutOpenWindow;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Component
@Scope("prototype")
public class TextPrompt {
	@Autowired
	private MenuManager menuManager;

	@Getter
	private final String title;
	@Getter
	private final String defaultInput;
	@Getter
	private final Player player;
	@Getter
	private Promise<String> promise;

	public Promise<String> show() {
		@NotNull
		UUID playerUUID = player.getUniqueId();
		Promise<String> promise = new Promise<String>();

		System.out.printf("%s, %s", menuManager, player);
		if (menuManager.playerHasPromptOpen(player.getUniqueId())) {
			throw new NotYetImplementedException("Queued text prompts");
			// TODO(Yevano): Implement queued text prompts.
		}

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		nmsPlayer.closeInventory();
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(1, Containers.ANVIL, new ChatMessage(title)));
		menuManager.setPlayerTextPrompt(playerUUID, this);
		return promise;
	}
}