package br.com.gitanalyzer.model.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.gitanalyzer.model.enums.FilteredEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositorySharedLinks {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

}
