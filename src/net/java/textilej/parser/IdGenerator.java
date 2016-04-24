package net.java.textilej.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IdGenerator {

	private Map<String, Integer> idGenerators = new HashMap<String, Integer>();
	private Set<String> anchorNames = new HashSet<String>();
	
	public String newId(String type, String text) {
		if (type == null) {
			type = "";
		}
		Integer current = idGenerators.get(type);
		if (current == null) {
			current = 0;
		}
		current = current + 1;
		
		idGenerators.put(type, current);
		
		String id = null;
		if (text != null) {
			id = convertToAnchor(text.trim());
			if (id.length() == 0) {
				id = type+'-'+current;
			}
		} else {
			id = type+'-'+current;
		}
		String template = id;
		int suffix = 1;
		while (!anchorNames.add(id)) {
			id = template + (++suffix);
		}
		return id;
	}
	
	private String convertToAnchor(String text) {
		String anchor = text.replaceAll("[^a-zA-Z0-9]", "");
		if (anchor.length() > 0 && Character.isDigit(anchor.charAt(0))) {
			anchor = 'a'+anchor;
		}
		return anchor;
	}
}
