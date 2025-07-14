package br.edu.fatecgru.insight_forge.controller;
/*
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")  // Define o caminho base para todos os endpoints
public class ProdutoController {
    /*
    private final ProdutoService produtoService;

    @Autowired
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // --- CRUD Endpoints ---

    // Criar novo produto (POST)
    @PostMapping
    public ResponseEntity<ProdutoEntity> criarProduto(@RequestBody ProdutoEntity produto) {
        ProdutoEntity novoProduto = produtoService.salvarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    // Listar todos os produtos (GET)
    @GetMapping
    public ResponseEntity<List<ProdutoEntity>> listarProdutos() {
        List<ProdutoEntity> produtos = produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    // Buscar produto por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorId(@PathVariable Long id) {
        Optional<ProdutoEntity> produto = produtoService.buscarPorId(id);
        return produto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar quantidade (PATCH)
    @PatchMapping("/{id}/quantidade")
    public ResponseEntity<Void> atualizarQuantidade(
            @PathVariable Long id,
            @RequestParam Integer novaQuantidade) {
        produtoService.atualizarQuantidade(id, novaQuantidade);
        return ResponseEntity.noContent().build();
    }

    // Deletar produto (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoEntity> atualizarProduto(
            @PathVariable Long id,
            @RequestBody ProdutoEntity produtoAtualizado) {

        if (produtoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        produtoAtualizado.setId(id);
        ProdutoEntity produtoSalvo = produtoService.salvarProduto(produtoAtualizado);
        return ResponseEntity.ok(produtoSalvo);
    }

}*/ //<--VERSÃO ANTIGA  package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // 🔄 CREATE - Criar novo produto
    @PostMapping
    public ResponseEntity<ProdutoEntity> criarProduto(@RequestBody ProdutoEntity produto) {
        ProdutoEntity novoProduto = produtoService.salvarOuAtualizarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    // 📄 READ - Listar todos os produtos
    @GetMapping
    public ResponseEntity<List<ProdutoEntity>> listarProdutos() {
        List<ProdutoEntity> produtos = produtoService.listarTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    // 🔍 READ - Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorId(@PathVariable Long id) {
        return produtoService.buscarProdutoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✏️ UPDATE - Atualizar produto por ID
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoEntity> atualizarProduto(
            @PathVariable Long id,
            @RequestBody ProdutoEntity produtoAtualizado) {

        try {
            ProdutoEntity atualizado = produtoService.atualizarProduto(id, produtoAtualizado);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ❌ DELETE - Remover produto por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProdutoPorId(id);
        return ResponseEntity.noContent().build();
    }
}



