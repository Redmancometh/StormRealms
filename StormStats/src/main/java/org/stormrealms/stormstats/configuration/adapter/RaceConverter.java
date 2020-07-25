package org.stormrealms.stormstats.configuration.adapter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormstats.configuration.pojo.Race;
import org.stormrealms.stormstats.configuration.pojo.RaceConfig;

@Converter
public class RaceConverter implements AttributeConverter<Race, String> {

	@Override
	public String convertToDatabaseColumn(Race race) {
		return race.getKey();
	}

	@Override
	public Race convertToEntityAttribute(String raceKey) {
		RaceConfig races = StormCore.getInstance().getContext().getBean(RaceConfig.class);
		return races.getRace(raceKey);
	}

}
