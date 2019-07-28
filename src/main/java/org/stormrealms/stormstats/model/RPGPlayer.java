package org.stormrealms.stormstats.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.DefaultsConfig;

import lombok.Data;

@Data
@Entity
@Table
public class RPGPlayer {
	@Autowired
	private transient ConfigManager<DefaultsConfig> cfgMan;
	@Id
	private UUID playerId;
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
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "player")
	private ClassData data;
	@ElementCollection(fetch = FetchType.EAGER, targetClass = java.lang.Integer.class)
	Map<UUID, Integer> questMap = new ConcurrentHashMap<UUID, Integer>();
}
