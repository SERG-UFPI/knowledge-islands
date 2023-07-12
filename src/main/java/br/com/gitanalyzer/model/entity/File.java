package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class File{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable=false)
	private String path;
	private int fileSize;
	private double totalKnowledge = 0;
	@ElementCollection
	private List<String> renamePaths = new ArrayList<String>();

	@javax.persistence.Transient
	private Set<Contributor> maintainers = new HashSet<Contributor>();

	public boolean isFile(String path) {
		List<String> paths = new ArrayList<String>();
		paths.add(this.path);
		paths.addAll(renamePaths);
		return paths.contains(path);
	}

	public File(String path) {
		super();
		this.path = path;
	}

}
