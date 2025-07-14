package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.EstoqueEntity;
import br.edu.fatecgru.insight_forge.service.EstoqueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // 🔄 CREATE
    @PostMapping
    public ResponseEntity<EstoqueEntity> registrarMovimento(@RequestBody EstoqueEntity movimento) {
        EstoqueEntity registrado = estoqueService.registrarOuAtualizarMovimento(movimento);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }

    // 📄 READ - Listar todos os movimentos
    @GetMapping
    public ResponseEntity<List<EstoqueEntity>> listarTodosMovimentos() {
        List<EstoqueEntity> movimentos = estoqueService.listarTodosMovimentos();
        return ResponseEntity.ok(movimentos);
    }

    // 🔍 READ - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueEntity> buscarPorId(@PathVariable Long id) {
        return estoqueService.buscarMovimentoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔎 READ - Listar por produto
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<EstoqueEntity>> listarMovimentosPorProduto(@PathVariable Long produtoId) {
        List<EstoqueEntity> movimentos = estoqueService.listarMovimentosPorProduto(produtoId);
        return ResponseEntity.ok(movimentos);
    }

    // ✏️ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<EstoqueEntity> atualizarMovimento(
            @PathVariable Long id,
            @RequestBody EstoqueEntity dadosAtualizados) {

        try {
            EstoqueEntity atualizado = estoqueService.atualizarMovimento(id, dadosAtualizados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMovimento(@PathVariable Long id) {
        estoqueService.deletarMovimentoPorId(id);
        return ResponseEntity.noContent().build();
    }
}
