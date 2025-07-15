package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.EstoqueEntity;
import br.edu.fatecgru.insight_forge.service.EstoqueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // CREATE
    @PostMapping("/criarEstoque")
    public ResponseEntity<EstoqueEntity> criarEstoque(@RequestBody EstoqueEntity estoque) {
        EstoqueEntity criado = estoqueService.salvarOuAtualizar(estoque);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // READ - Listar todos ou por produto
    // Endpoint para listar todos os estoques ou filtrar por produto, contendo o ID do produto como parâmetro opcional {produtoId}
    @GetMapping("/listarEstoques")
    public ResponseEntity<List<EstoqueEntity>> listarEstoquesPorProduto(@RequestParam(required = false) Long produtoId) {
        if (produtoId != null) {
            return ResponseEntity.ok(estoqueService.listarPorProduto(produtoId));
        }
        return ResponseEntity.ok(estoqueService.listarTodos());
    }

    // READ - Buscar por ID
    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<EstoqueEntity> buscarPorId(@PathVariable Long id) {
        return estoqueService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/atualizarEstoque/{id}")
    public ResponseEntity<EstoqueEntity> atualizarEstoque(
            @PathVariable Long id,
            @RequestBody EstoqueEntity dadosAtualizados) {
        try {
            EstoqueEntity atualizado = estoqueService.atualizar(id, dadosAtualizados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE
    @DeleteMapping("/deletarEstoque/{id}")
    public ResponseEntity<Void> deletarEstoque(@PathVariable Long id) {
        estoqueService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }
}