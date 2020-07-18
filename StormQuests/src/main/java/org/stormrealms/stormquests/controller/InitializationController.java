package org.stormrealms.stormquests.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormquests.pojo.Quest;

import com.google.gson.Gson;

@Controller
public class InitializationController {

	@Autowired
	@Qualifier("quest-parser")
	private Gson questParser;
	@Autowired
	@Qualifier("quests")
	private Map<Integer, Quest> quests;
	
	@PostConstruct
	public void registerQuests() {
		System.out.println("REGISTERING QUESTS");
		try (Stream<Path> pathStream = Files.walk(Paths.get("config/quests"))) {
			pathStream.filter(path1 -> path1.toString().endsWith(".json")).forEach(p -> {
				System.out.println("FOUND FILE " + p.toFile());
				try (FileReader reader = new FileReader(p.toFile())) {

					Quest quest = questParser.fromJson(reader, Quest.class);
					quests.put(quest.getId(), quest);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
