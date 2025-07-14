package br.edu.fatecgru.insight_forge.repository;

import br.edu.fatecgru.insight_forge.model.FornecedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FornecedorRepository extends JpaRepository<FornecedorEntity, Long> {
}