package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@Column(nullable=false, length=500)
	private String path;
	@JsonIgnore
	@Column(length=500)
	private String name;
	@JsonIgnore
	@Column(length=500)
	private String sha;
	@JsonIgnore
	@Column(length=500)
	private String url;
	@JsonIgnore
	@Column(length=500)
	private String htmlUrl;
	@JsonIgnore
	@Column(length=500)
	private String downloadUrl;
	@JsonIgnore
	@Column(length=500)
	private String gitUrl;
	@JsonIgnore
	private String contentEncoded;
	@JsonIgnore
	private String contentDecoded;
	@JsonIgnore
	private String encoding;
	@JsonIgnore
	@Transient
	private List<Commit> commits;
	@JsonIgnore
	@Transient
	private GitRepository repository;
	private int size;
	@JsonIgnore
	@ElementCollection
	private List<String> renamePaths = new ArrayList<String>();
	@JsonIgnore
	@OneToMany(mappedBy="file", cascade = CascadeType.REMOVE)
	private List<FileVersion> versions;
	private String language;
	
	public boolean isFile(String path) {
		List<String> paths = new ArrayList<String>();
		paths.add(this.path);
		paths.addAll(renamePaths);
		return paths.contains(path);
	}

	public File(String path, int size) {
		super();
		this.path = path;
		this.size = size;
	}

	public File(String path) {
		super();
		this.path = path;
	}

}
