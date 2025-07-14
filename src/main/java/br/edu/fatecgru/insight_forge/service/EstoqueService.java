package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.EstoqueEntity;
import br.edu.fatecgru.insight_forge.repository.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EstoqueService {

//    private final EstoqueRepository estoqueRepository;
//
//    public EstoqueEntity registrarMovimento(EstoqueEntity movimento) {
//        return estoqueRepository.save(movimento);
//    }
//
//    public List<EstoqueEntity> listarMovimentosPorProduto(Long produtoId) {
//        return estoqueRepository.findByProdutoId(produtoId);
//    }

    private final EstoqueRepository estoqueRepository;

    // 🔄 CREATE ou UPDATE
    public EstoqueEntity registrarOuAtualizarMovimento(EstoqueEntity movimento) {
        return estoqueRepository.save(movimento);
    }

    // 📄 READ - Listar todos os movimentos
    public List<EstoqueEntity> listarTodosMovimentos() {
        return estoqueRepository.findAll();
    }

    // 🔍 READ - Buscar movimento por ID
    public Optional<EstoqueEntity> buscarMovimentoPorId(Long id) {
        return estoqueRepository.findById(id);
    }

    // 🔍 READ - Listar por produto
    public List<EstoqueEntity> listarMovimentosPorProduto(Long produtoId) {
        return estoqueRepository.findByProdutoId(produtoId);
    }

    // ✏️ UPDATE - Atualizar movimento existente
    public EstoqueEntity atualizarMovimento(Long id, EstoqueEntity dadosAtualizados) {
        return estoqueRepository.findById(id).map(movimento -> {
            movimento.setProduto(dadosAtualizados.getProduto());
            movimento.setTipoMovimento(dadosAtualizados.getTipoMovimento());
            movimento.setQuantidade(dadosAtualizados.getQuantidade());
            movimento.setDataMovimento(dadosAtualizados.getDataMovimento());
            return estoqueRepository.save(movimento);
        }).orElseThrow(() -> new RuntimeException("Movimento de estoque não encontrado com ID: " + id));
    }

    // ❌ DELETE - Remover movimento por ID
    public void deletarMovimentoPorId(Long id) {
        estoqueRepository.deleteById(id);
    }
}