package org.workflow.Classes;

import java.util.LinkedList;
import java.util.List;

public class Person {

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getQualifications() {
		return qualifications;
	}

	public void setQualifications(List<String> qualifications) {
		this.qualifications = qualifications;
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
