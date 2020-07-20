package org.stormrealms.stormcombat.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class RegenConfig {
	private Map<Integer, Double> regenCoefficients;
}
