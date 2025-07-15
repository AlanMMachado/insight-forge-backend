package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // 🔄 CREATE - Criar novo produto
    @PostMapping("/criarProduto")
    public ResponseEntity<ProdutoEntity> criarProduto(@RequestBody ProdutoEntity produto) {
        ProdutoEntity novoProduto = produtoService.salvarOuAtualizarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    // 📄 READ - Listar todos os produtos
    @GetMapping("/listarProdutos")
    public ResponseEntity<List<ProdutoEntity>> listarProdutos() {
        List<ProdutoEntity> produtos = produtoService.listarTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    // 🔍 READ - Buscar produto por ID
    @GetMapping("/buscarProdutoPorId/{id}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorId(@PathVariable Long id) {
        return produtoService.buscarProdutoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✏️ UPDATE - Atualizar produto por ID
    @PutMapping("/atualizarProduto/{id}")
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
    @DeleteMapping("/deletarProduto/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProdutoPorId(id);
        return ResponseEntity.noContent().build();
    }
}



