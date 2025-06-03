package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.SharedLink;

@Repository
public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {

	SharedLink findByLink(String link);
	List<SharedLink> findByConversationIsNullAndErrorIsNull();
	List<SharedLink> findByConversationIsNotNullAndErrorIsNull();

}
