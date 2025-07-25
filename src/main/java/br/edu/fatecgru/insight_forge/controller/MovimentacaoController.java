package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.service.MovimentacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping("/criarMovimentacao")
    public ResponseEntity<MovimentacaoEntity> criarMovimentacao(@RequestBody MovimentacaoEntity movimentacao) {
        MovimentacaoEntity nova = movimentacaoService.salvarOuAtualizar(movimentacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(nova);
    }

    @GetMapping("/listarMovimentacoes")
    public ResponseEntity<List<MovimentacaoEntity>> listarMovimentacoes() {
        List<MovimentacaoEntity> lista = movimentacaoService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<MovimentacaoEntity> buscarPorId(@PathVariable Long id) {
        return movimentacaoService.buscarPorMovimentacaoId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filtrarPorTipo")
    public ResponseEntity<List<MovimentacaoEntity>> filtrarPorTipo(@RequestParam String tipo) {
        List<MovimentacaoEntity> lista = movimentacaoService.listarPorTipoMovimentacao(tipo);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/filtrarPorData")
    public ResponseEntity<List<MovimentacaoEntity>> filtrarPorData(@RequestParam String dataInicio, @RequestParam String dataFim) {
        LocalDate inicio = LocalDate.parse(dataInicio);
        LocalDate fim = LocalDate.parse(dataFim);
        List<MovimentacaoEntity> lista = movimentacaoService.listarPorIntervaloDeDatas(inicio, fim);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/filtrarPorProduto")
    public ResponseEntity<List<MovimentacaoEntity>> filtrarPorProduto(@RequestParam Long produtoId) {
        List<MovimentacaoEntity> lista = movimentacaoService.listarPorProdutoId(produtoId);
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/atualizarMovimentacao/{id}")
    public ResponseEntity<MovimentacaoEntity> atualizarMovimentacao(@PathVariable Long id, @RequestBody MovimentacaoEntity dadosAtualizados) {
        try {
            MovimentacaoEntity atualizado = movimentacaoService.atualizar(id, dadosAtualizados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/deletarMovimentacao/{id}")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long id) {
        movimentacaoService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para importar movimentações via arquivo Excel
    @PostMapping("/importarMovimentacoes")
    public ResponseEntity<String> importarMovimentacoes(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo para importar.");
        }
        try {
            File tempFile = File.createTempFile("movimentacoes", ".xlsx");
            file.transferTo(tempFile);
            movimentacaoService.importarMovimentacoesAsync(tempFile);
            return ResponseEntity.ok("Movimentações importadas com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao importar movimentações: " + e.getMessage());
        }
    }

    @GetMapping("/exportarMovimentacoesPorProduto")
    public ResponseEntity<byte[]> exportarMovimentacoesPorProduto(@RequestParam Long produtoId) {
        try {
            byte[] arquivo = movimentacaoService.exportarMovimentacoesPorProduto(produtoId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=movimentacoes_produto.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(arquivo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/exportarMovimentacoesPorCategoria")
    public ResponseEntity<byte[]> exportarMovimentacoesPorCategoria(@RequestParam String categoria) {
        try {
            byte[] arquivo = movimentacaoService.exportarMovimentacoesPorCategoria(categoria);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=movimentacoes_categoria.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(arquivo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}