package org.stormrealms.stormstats.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.DefaultsConfig;

import lombok.Data;

@Entity
@Data
public class RPGPlayer {
	@Autowired
	private ConfigManager<DefaultsConfig> cfgMan;
	@Column
	private double experience;
	@Column
	private int level;
	@Column
	private int str;
	@Column
	private int intel;
	@Column
	private int spi;
	@Column
	private int agi;
	@Column
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	Map<UUID, Integer> questMap = new ConcurrentHashMap<UUID, Integer>();
}
