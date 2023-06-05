package br.com.gitanalyzer.model.entity;

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

	public TruckFactorProcess(StageEnum stage) {
		super();
		this.stage = stage;
	}

}
