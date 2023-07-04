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

import org.hibernate.annotations.CreationTimestamp;

import br.com.gitanalyzer.dto.TruckFactorProcessDTO;
import br.com.gitanalyzer.enums.StageEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TruckFactorProcess {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;
	@Enumerated(EnumType.STRING)
	private StageEnum stage;
	@OneToOne
	private TruckFactor truckFactor;
	@ManyToOne(targetEntity = br.com.gitanalyzer.model.entity.User.class)
	private User user;
	private String repositoryUrl;

	public TruckFactorProcess(StageEnum stage, User user, 
			String repositoryUrl) {
		super();
		this.stage = stage;
		this.user = user;
		this.repositoryUrl = repositoryUrl;
	}

	public TruckFactorProcessDTO toDTO() {
		SimpleDateFormat fmt = new SimpleDateFormat("YYYY-MM-DD HH:mm");
		return TruckFactorProcessDTO.builder()
				.repositoryUrl(repositoryUrl)
				.endDate(fmt.format(endDate))
				.id(id)
				.stage(stage.getName())
				.startDate(fmt.format(startDate))
				.truckFactor(truckFactor!=null?truckFactor.toDto():null)
				.user(user.toDTO()).build();
	}

}
