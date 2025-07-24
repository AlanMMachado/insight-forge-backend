package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping("/criarProduto")
    public ResponseEntity<ProdutoEntity> criarProduto(@RequestBody ProdutoEntity produto) {
        ProdutoEntity novoProduto = produtoService.salvarOuAtualizarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    @GetMapping("/listarProdutos")
    public ResponseEntity<List<ProdutoEntity>> listarProdutos() {
        List<ProdutoEntity> produtos = produtoService.listarTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscarProdutoPorId/{id}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorId(@PathVariable Long id) {
        return produtoService.buscarProdutoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscarProdutoPorCategoria/{categoria}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorCategoria(@RequestParam String categoria) {
        List<ProdutoEntity> produtos = produtoService.buscarProdutosPorCategoria(categoria);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos.get(0)); // Retorna o primeiro produto encontrado
    }

    @GetMapping("/buscarProdutoPorNome/{nome}")
    public ResponseEntity<ProdutoEntity> buscarProdutoPorNome(@RequestParam String nome){
        List<ProdutoEntity> produtos = produtoService.buscarProdutosPorNome(nome);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos.get(0)); // Retorna o primeiro produto encontrado
    }

    @GetMapping("/buscarProdutosAtivos")
    public ResponseEntity<List<ProdutoEntity>> buscarProdutosAtivos(@RequestParam Boolean ativo) {
        List<ProdutoEntity> produtos = produtoService.buscarProdutosAtivos(ativo);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @PostMapping("/importarProdutos")
    public ResponseEntity<String> importarProdutos(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo para importar.");
        }
        try {
            File tempFile = File.createTempFile("produtos", ".xlsx");
            file.transferTo(tempFile);
            produtoService.importarProdutosAsync(tempFile);
            return ResponseEntity.ok("Produtos importados com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao importar produtos: " + e.getMessage());
        }
    }

    @GetMapping("/exportarProdutos")
    public ResponseEntity<byte[]> exportarProdutos() {
        try {
            byte[] arquivo = produtoService.exportarProdutos();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=produtos.xlsx")
                    .body(arquivo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/atualizarProduto/{id}")
    public ResponseEntity<ProdutoEntity> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoEntity produtoAtualizado) {
        try {
            ProdutoEntity atualizado = produtoService.atualizarProduto(id, produtoAtualizado);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/deletarProduto/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProdutoPorId(id);
        return ResponseEntity.noContent().build();
    }
}



