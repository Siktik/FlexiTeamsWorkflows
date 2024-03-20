package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class ResourceType {

	//used for resources of this type in case this resource is limited
	private int idCounter = 0;
	private String name;
	private boolean unlimited = false;
	private int numberOfOccasions = 0;

	public ResourceType(String name, boolean unlimited, int numberOfOccasions) {
		this.name = name;

		setUnlimited(unlimited);
		setNumberOfOccasions(numberOfOccasions);		//System.out.println(this);
	}

	public String retrieveID() {
		return name + (idCounter++);
	}


	@Override
	public String toString() {
		return "ResourceType{" +
				"idCounter=" + idCounter +
				", name='" + name + '\'' +
				", unlimited=" + unlimited +
				", numberOfOccasions=" + numberOfOccasions +
				'}';
	}
}
