package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Person {

	/**
	 * use unique identifiers
	 */

	private static int idCounter = 0;
	private int id;

	private String name;
	private List<String> qualifications;

	public Person(String name, List<String> qualifications) {
		this.id = idCounter++;
		this.qualifications = qualifications;
		this.name = name;
		//System.out.println(this);
	}

	public void addQualification(String qualificationName) {
		this.qualifications.add(qualificationName);
	}



	@Override
	public String toString() {
		return (
			"Person{" +
			"id=" +
			id +
			", name='" +
			name +
			'\'' +
			", qualifications=" +
			qualifications +
			'}'
		);
	}
}
