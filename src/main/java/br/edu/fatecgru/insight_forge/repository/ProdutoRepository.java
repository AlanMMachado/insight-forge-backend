package br.edu.fatecgru.insight_forge.repository;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {
    // Retorna uma lista de produtos por categoria, ativo e nome
    List<ProdutoEntity> findByCategoria(String categoria);
    List<ProdutoEntity> findByAtivo(Boolean ativo);
    List<ProdutoEntity> findByNomeContainingIgnoreCase(String nome); // Use ContainingIgnoreCase para buscar ignorando letras maiúsculas/minúsculas.
}