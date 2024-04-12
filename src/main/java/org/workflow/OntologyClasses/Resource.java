package org.workflow.OntologyClasses;

import lombok.Getter;
import org.workflow.OntologyClasses.ResourceType;

@Getter
public class Resource {

	private String id;

	private ResourceType type;

	public Resource(ResourceType type) {
		this.id = type.retrieveID();
		this.type = type;
		//System.out.println(this);
	}

	@Override
	public String toString() {
		return "Resource{" + "id=" + id + ", type='" + type + '\'' + '}';
	}
}
