package br.com.gitanalyzer.model.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.gitanalyzer.dto.GitRepositoryVersionProcessDTO;
import br.com.gitanalyzer.enums.GitRepositoryVersionProcessStageEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryVersionProcess {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;
	@Enumerated(EnumType.STRING)
	private GitRepositoryVersionProcessStageEnum stage;
	@OneToOne
	private GitRepositoryVersion gitRepositoryVersion;
	@JsonIgnore
	@ManyToOne(targetEntity = br.com.gitanalyzer.model.entity.User.class)
	private User user;
	@NotNull
	private String repositoryUrl;
	
	public GitRepositoryVersionProcess(GitRepositoryVersionProcessStageEnum stage, User user,
			@NotNull String repositoryUrl) {
		super();
		startDate = new Date();
		this.stage = stage;
		this.user = user;
		this.repositoryUrl = repositoryUrl;
	}
	public GitRepositoryVersionProcessDTO toDTO() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return GitRepositoryVersionProcessDTO.builder()
				.repositoryUrl(repositoryUrl)
				.endDate(endDate!=null?fmt.format(endDate):null)
				.id(id)
				.idGitRepositoryVersion(gitRepositoryVersion!=null?gitRepositoryVersion.getId():null)
				.stage(stage.getName())
				.startDate(startDate!=null?fmt.format(startDate):null)
				.build();
	}
}
