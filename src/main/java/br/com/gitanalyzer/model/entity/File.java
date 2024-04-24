package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import br.com.gitanalyzer.model.Commit;
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
	private String name;
	private String sha;
	private String url;
	private String htmlUrl;
	private String downloadUrl;
	private String gitUrl;
	private String contentEncoded;
	private String contentDecoded;
	private String encoding;
	@Transient
	private List<Commit> commits;
	@Transient
	private GitRepository repository;
	private int size;
	
	private double totalKnowledge = 0;
	@ElementCollection
	private List<String> renamePaths = new ArrayList<String>();

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
