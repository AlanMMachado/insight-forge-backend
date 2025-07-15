package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.service.MovimentacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    // 🔄 CREATE
    @PostMapping("/criarMovimentacao")
    public ResponseEntity<MovimentacaoEntity> criarMovimentacao(@RequestBody MovimentacaoEntity movimentacao) {
        MovimentacaoEntity nova = movimentacaoService.salvarOuAtualizar(movimentacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(nova);
    }

    // 📄 READ - Listar todas
    @GetMapping("/listarMovimentacoes")
    public ResponseEntity<List<MovimentacaoEntity>> listarMovimentacoes() {
        List<MovimentacaoEntity> lista = movimentacaoService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    // 🔍 READ - Buscar por ID
    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<MovimentacaoEntity> buscarPorId(@PathVariable Long id) {
        return movimentacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✏️ UPDATE
    @PutMapping("/atualizarMovimentacao/{id}")
    public ResponseEntity<MovimentacaoEntity> atualizarMovimentacao(
            @PathVariable Long id,
            @RequestBody MovimentacaoEntity dadosAtualizados) {
        try {
            MovimentacaoEntity atualizado = movimentacaoService.atualizar(id, dadosAtualizados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ❌ DELETE
    @DeleteMapping("/deletarMovimentacao/{id}")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long id) {
        movimentacaoService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }
}