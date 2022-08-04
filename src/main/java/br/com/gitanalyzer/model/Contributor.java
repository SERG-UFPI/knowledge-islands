package br.com.gitanalyzer.model;

import java.util.Objects;
import java.util.Set;

import lombok.Data;

@Data
public class Contributor {

	private String name;
	private String email;

	private int numberFilesAuthor;
	private double sumFileImportance;
	private Set<Contributor> alias;

	public Contributor(String name, String email, Project project) {
		super();
		this.name = name;
		this.email = email;
	}

	public Contributor(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}

	public Contributor() {
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contributor other = (Contributor) obj;
		return Objects.equals(email, other.email) && Objects.equals(name, other.name);
	}
}
