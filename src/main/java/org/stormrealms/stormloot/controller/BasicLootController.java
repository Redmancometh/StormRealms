package org.stormrealms.stormloot.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormloot.configuration.pojo.MasterLootConfig;

@Component
public class BasicLootController implements LootSelector {
	@Autowired
	private MasterLootConfig masterLootCfg;

	@PostConstruct
	public void printMaster() {
		System.out.println("Proceed to echo master loot config!");
		System.out.println(masterLootCfg);
	}
}
