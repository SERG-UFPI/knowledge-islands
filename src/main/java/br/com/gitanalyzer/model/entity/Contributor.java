package br.com.gitanalyzer.model.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import br.com.gitanalyzer.dto.ContributorDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Contributor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	private Double percentOfFilesAuthored;
	private int numberFilesAuthor;
	private boolean active;
	@ManyToMany(cascade = CascadeType.REMOVE)
	private Set<File> filesAuthor = new HashSet<>();
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<Contributor> alias;

	public Contributor(String name, String email, Double percentOfFilesAuthored) {
		super();
		this.name = name;
		this.email = email;
		this.percentOfFilesAuthored = percentOfFilesAuthored;
	}

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

	public ContributorDTO toDto() {
		return ContributorDTO.builder()
				.id(id)
				.email(email)
				.name(name)
				.numberFilesAuthor(numberFilesAuthor)
				.percentOfFilesAuthored(percentOfFilesAuthored)
				.active(active)
				.build();
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
