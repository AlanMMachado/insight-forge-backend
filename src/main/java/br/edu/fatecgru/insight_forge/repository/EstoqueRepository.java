package br.edu.fatecgru.insight_forge.repository;

import br.edu.fatecgru.insight_forge.model.EstoqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstoqueRepository extends JpaRepository<EstoqueEntity, Long> {
    List<EstoqueEntity> findByProdutoId(Long produtoId);
}