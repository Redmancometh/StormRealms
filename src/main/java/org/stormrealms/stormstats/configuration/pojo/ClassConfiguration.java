package org.stormrealms.stormstats.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ClassConfiguration {
	private Map<String, ClassInformation> classMap = new HashMap();
}
