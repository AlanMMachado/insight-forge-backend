package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoEntity salvarOuAtualizarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    public List<ProdutoEntity> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    public Optional<ProdutoEntity> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public void deletarProdutoPorId(Long id) {
        produtoRepository.deleteById(id);
    }

    public ProdutoEntity atualizarProduto(Long id, ProdutoEntity dadosAtualizados) {
        return produtoRepository.findById(id).map(produto -> {
            produto.setNome(dadosAtualizados.getNome());
            produto.setDescricao(dadosAtualizados.getDescricao());
            produto.setPreco(dadosAtualizados.getPreco());
            produto.setFornecedores(dadosAtualizados.getFornecedores());
            produto.setAtivo(dadosAtualizados.getAtivo());
            return produtoRepository.save(produto);
        }).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }
}