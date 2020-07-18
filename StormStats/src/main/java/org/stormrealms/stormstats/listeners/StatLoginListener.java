package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.menus.TextPrompt;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.model.RPGCharacter;

/**
 * 
 * @author Redmancometh
 *
 */
@Component
public class StatLoginListener implements Listener {
	@Autowired
	@Qualifier("needs-character")
	private List<UUID> characterless;
	@Autowired
    private StatRepo repo;
    
    @Autowired private MenuManager menuManager;

	@EventHandler
	public void onChat(PlayerJoinEvent e) {
		System.out.println("LOGIN2");
        RPGCharacter character = new RPGCharacter();
    
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			Session session = repo.getSubDB().getFactory().openSession();
			session.save(character);
			character.setCharacterName(UUID.randomUUID().toString());
			rpgPlayer.getCharacters().add(character);
			repo.saveAndPurge(rpgPlayer, e.getPlayer().getUniqueId());
        });
        
        // TODO: JUST TESTING DOESN'T WORK YET
        // menuManager.prompt("Character Name", "Dildo Shaggins", e.getPlayer())
        //     .then(chosenName -> {
        //         System.out.printf("Player chose the name %s", chosenName);
        //     });
	}
}
