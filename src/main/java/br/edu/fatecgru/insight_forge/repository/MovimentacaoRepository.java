package br.edu.fatecgru.insight_forge.repository;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEntity, Long> {
}
