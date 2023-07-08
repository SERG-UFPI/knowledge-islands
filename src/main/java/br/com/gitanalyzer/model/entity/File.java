package br.com.gitanalyzer.model.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable=false)
	private String path;
	private String extension;
	private int fileSize;
	private int numberOfCommits;
	private double totalKnowledge;
	@ElementCollection
	private List<String> renamedPaths;

}
