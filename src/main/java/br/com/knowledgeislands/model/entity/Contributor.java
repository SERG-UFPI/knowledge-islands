package br.com.knowledgeislands.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Contributor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	private boolean active;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<Contributor> alias;
	@OneToMany(mappedBy="contributor", cascade = CascadeType.REMOVE)
	private List<AttemptSendEmail> attemptsSendEmail;
	@JsonIgnore
	@OneToOne(cascade=CascadeType.REMOVE)
	private ContributorGenAiUse contributorGenAiUse;

	public Contributor(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}

	public List<Contributor> contributorAlias(){
		List<Contributor> contributorAlias = new ArrayList<>();
		contributorAlias.add(this);
		if(alias != null && !alias.isEmpty()) {
			contributorAlias.addAll(alias);
		}
		return contributorAlias;
	}

	public void clearAlias() {
		if(this.alias != null && !this.alias.isEmpty()) {
			this.alias.clear();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contributor other = (Contributor) obj;
		return Objects.equals(email, other.email) && Objects.equals(name, other.name);
	}

}
