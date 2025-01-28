package br.com.knowledgeislands.model.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContributorVersion implements Comparable<ContributorVersion>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private double percentOfFilesAuthored;
	private int numberFilesAuthor;
	@JsonIgnore
	@ManyToMany(cascade = CascadeType.REMOVE)
	private Set<File> filesAuthor = new HashSet<>();
	@ManyToOne
	private Contributor contributor;
	@Transient
	private List<String> filesAuthorPath;
	public ContributorVersion(Contributor contributor) {
		super();
		this.contributor = contributor;
		percentOfFilesAuthored = 0.0;
	}
	@Override
	public int compareTo(ContributorVersion other) {
		return Integer.compare(this.numberFilesAuthor, other.numberFilesAuthor);
	}
}
