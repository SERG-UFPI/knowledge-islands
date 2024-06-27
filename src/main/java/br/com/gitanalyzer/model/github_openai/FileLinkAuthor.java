package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.SharedLink;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class FileLinkAuthor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
//	@OneToOne
//	private AuthorFile authorFile;
	@ElementCollection
	private List<String> linesCopied;
	@ManyToOne
	private SharedLink sharedLink;
	@OneToOne
	private File file;
	private boolean dataExtracted;

	public FileLinkAuthor() {
	//	this.authorFile = new AuthorFile();
		this.linesCopied = new ArrayList<>();
	}

}
