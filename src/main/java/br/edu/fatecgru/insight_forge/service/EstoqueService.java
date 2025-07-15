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

    private final EstoqueRepository estoqueRepository;

    public EstoqueEntity salvarOuAtualizar(EstoqueEntity estoque) {
        return estoqueRepository.save(estoque);
    }

    public List<EstoqueEntity> listarTodos() {
        return estoqueRepository.findAll();
    }

    public Optional<EstoqueEntity> buscarPorId(Long id) {
        return estoqueRepository.findById(id);
    }

    public List<EstoqueEntity> listarPorProduto(Long produtoId) {
        return estoqueRepository.findByProdutoId(produtoId);
    }

    public EstoqueEntity atualizar(Long id, EstoqueEntity dadosAtualizados) {
        return estoqueRepository.findById(id).map(estoque -> {
            estoque.setProduto(dadosAtualizados.getProduto());
            estoque.setQuantidade(dadosAtualizados.getQuantidade());
            estoque.setDataCadastro(dadosAtualizados.getDataCadastro());
            return estoqueRepository.save(estoque);
        }).orElseThrow(() -> new RuntimeException("Estoque não encontrado com ID: " + id));
    }

    public void deletarPorId(Long id) {
        estoqueRepository.deleteById(id);
    }
}