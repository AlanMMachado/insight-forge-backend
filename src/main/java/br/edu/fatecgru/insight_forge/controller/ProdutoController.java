package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.converter.ProdutoConverter;
import br.edu.fatecgru.insight_forge.dto.ProdutoDTO;
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoConverter produtoConverter;

    public ProdutoController(ProdutoService produtoService, ProdutoConverter produtoConverter) {
        this.produtoService = produtoService;
        this.produtoConverter = produtoConverter;
    }

    @PostMapping("/criarProduto")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ProdutoDTO> criarProduto(@RequestBody ProdutoEntity produto) {
        ProdutoEntity novoProduto = produtoService.salvarOuAtualizarProduto(produto);
        ProdutoDTO dto = produtoConverter.toDTO(novoProduto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/listarProdutos")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ProdutoDTO>> listarProdutos() {
        List<ProdutoDTO> produtos = produtoService.listarTodosProdutosDTO();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/listarCategorias")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = produtoService.listarCategorias();
        return ResponseEntity.ok(categorias); // Retorna lista vazia se não houver categorias
    }

    @GetMapping("/buscarProdutoPorId/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ProdutoDTO> buscarProdutoPorId(@PathVariable Long id) {
        return produtoService.buscarProdutoPorIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscarProdutoPorCategoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ProdutoDTO>> buscarProdutoPorCategoria(@RequestParam String categoria) {
        List<ProdutoDTO> produtos = produtoService.buscarProdutosPorCategoriaDTO(categoria);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscarProdutoPorNome/{nome}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ProdutoDTO>> buscarProdutoPorNome(@RequestParam String nome){
        List<ProdutoDTO> produtos = produtoService.buscarProdutosPorNomeDTO(nome);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscarProdutosAtivos")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ProdutoDTO>> buscarProdutosAtivos(@RequestParam Boolean ativo) {
        List<ProdutoDTO> produtos = produtoService.buscarProdutosAtivosDTO(ativo);
        if (produtos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @PostMapping("/importarProdutos")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ProdutoDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoEntity produtoAtualizado) {
        try {
            ProdutoEntity atualizado = produtoService.atualizarProduto(id, produtoAtualizado);
            ProdutoDTO dto = produtoConverter.toDTO(atualizado);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/deletarProduto/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProdutoPorId(id);
        return ResponseEntity.noContent().build();
    }
}
