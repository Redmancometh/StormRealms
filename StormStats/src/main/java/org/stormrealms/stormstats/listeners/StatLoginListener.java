package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;
import org.bukkit.event.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.StatMiscConfig;
import org.stormrealms.stormstats.data.StatRepo;

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
	@Qualifier("stat-config")
	private ConfigManager<StatMiscConfig> miscCfg;
	@Autowired
	private StatRepo repo;
	@Autowired
	private AutowireCapableBeanFactory factory;

}
