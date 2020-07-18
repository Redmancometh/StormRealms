package org.stormrealms.stormmenus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MenuTemplate implements Iterable<Pane> {
	private List<Pane> panes = new ArrayList();

	public List<Pane> getPanes() {
		return panes;
	}

	public boolean hasPane(int slot) {
		for (Pane pane : panes)
			if (pane.getIndexes().contains(slot))
				return true;
		return false;
	}

	public void setPanes(List<Pane> panes) {
		this.panes = panes;
	}

	@Override
	public Iterator<Pane> iterator() {
		return panes.iterator();
	}

	@Override
	public void forEach(Consumer<? super Pane> action) {
		panes.forEach(action);
	}

}
