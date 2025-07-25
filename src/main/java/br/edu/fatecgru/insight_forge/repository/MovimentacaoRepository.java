package br.edu.fatecgru.insight_forge.repository;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEntity, Long> {
    List<MovimentacaoEntity> findByTipoMovimentacao(String tipoMovimentacao);
    List<MovimentacaoEntity> findByDataMovimentacaoBetween(LocalDate startDate, LocalDate endDate);
    List<MovimentacaoEntity> findByProdutoId(Long produtoId);
}
