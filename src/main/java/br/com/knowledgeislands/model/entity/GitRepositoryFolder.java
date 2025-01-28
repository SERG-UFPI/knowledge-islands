package br.com.knowledgeislands.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import br.com.knowledgeislands.utils.LongToStringSerializer;
import lombok.Data;

@Entity
@Data
public class GitRepositoryFolder {
	@JsonSerialize(using = LongToStringSerializer.class)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length=1000)
	private String label;
	@Column(length=1000)
	private String path;
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<GitRepositoryFolder> children;
	private boolean folder;
	@Transient
	private TruckFactor truckFactor;
	@Transient
	private List<FileVersion> files;

	public GitRepositoryFolder() {
		super();
	}
	public GitRepositoryFolder(String label, String path) {
		this.label=label;
		this.path = path;
		children = new ArrayList<>();
	}
}
