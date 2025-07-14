package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {
/*
    private final ProdutoRepository produtoRepository;

    // Injeção via construtor (melhor prática)
    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // --- CRUD Básico ---

    // Salvar/Atualizar produto
    public ProdutoEntity salvarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    // Buscar todos os produtos
    public List<ProdutoEntity> listarTodos() {
        return produtoRepository.findAll();
    }

    // Buscar por ID
    public Optional<ProdutoEntity> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    // Deletar produto
    public void deletarProduto(Long id) {
        produtoRepository.deleteById(id);
    }

    // --- Métodos Customizados (exemplo) ---

    // Atualizar quantidade em estoque
    public void atualizarQuantidade(Long id, Integer novaQuantidade) {
        produtoRepository.findById(id).ifPresent(produto -> {
            produto.setQuantidade(novaQuantidade);
            produtoRepository.save(produto);
        });
    }
*/

        private final ProdutoRepository produtoRepository;

        /*public ProdutoEntity salvarProduto(ProdutoEntity produto) {
            return produtoRepository.save(produto);
        }

        public List<ProdutoEntity> listarTodos() {
            return produtoRepository.findAll();
        }

        public Optional<ProdutoEntity> buscarPorId(Long id) {
            return produtoRepository.findById(id);
        }*/

    // 🔄 CREATE ou UPDATE
    public ProdutoEntity salvarOuAtualizarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    // 📄 READ - Buscar todos os produtos
    public List<ProdutoEntity> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    // 🔍 READ - Buscar produto por ID
    public Optional<ProdutoEntity> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    // ❌ DELETE - Remover produto por ID
    public void deletarProdutoPorId(Long id) {
        produtoRepository.deleteById(id);
    }

    // ✏️ UPDATE - Atualizar produto existente
    public ProdutoEntity atualizarProduto(Long id, ProdutoEntity dadosAtualizados) {
        return produtoRepository.findById(id).map(produto -> {
            produto.setNome(dadosAtualizados.getNome());
            produto.setDescricao(dadosAtualizados.getDescricao());
            produto.setPreco(dadosAtualizados.getPreco());
            produto.setFornecedor(dadosAtualizados.getFornecedor());
            produto.setDataCadastro(dadosAtualizados.getDataCadastro());
            produto.setAtivo(dadosAtualizados.getAtivo());
            return produtoRepository.save(produto);
        }).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }

}