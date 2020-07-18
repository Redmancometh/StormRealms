package org.stormrealms.stormcore.config.pojo;

import java.util.Collection;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@Component
public class PluginLoadTaskContainer {
	private Multimap<Integer, PluginLoadTask> tasks = HashMultimap.create();
	private int currentPass = 0;

	public Collection<PluginLoadTask> getPass(int x) {
		return tasks.get(x);
	}

	public boolean hasPass(int x) {
		return tasks.containsKey(x);
	}

	public Collection<PluginLoadTask> getNextPass() {
		Collection<PluginLoadTask> puginsPass = tasks.get(currentPass);
		currentPass++;
		return puginsPass;
	}

	public void addTask(PluginLoadTask task) {
		System.out.println(task.getConfig());
		tasks.put(task.getConfig().getLoadOrder(), task);
	}

}
