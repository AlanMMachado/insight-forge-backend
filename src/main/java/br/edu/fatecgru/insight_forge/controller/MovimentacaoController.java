package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.dto.MovimentacaoDTO;
import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.service.MovimentacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<MovimentacaoDTO> criarMovimentacao(@RequestBody MovimentacaoEntity movimentacao) {
        MovimentacaoEntity nova = movimentacaoService.salvarOuAtualizar(movimentacao);
        MovimentacaoDTO dto = movimentacaoService.toDTO(nova);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/listarMovimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MovimentacaoDTO>> listarMovimentacoes() {
        List<MovimentacaoDTO> lista = movimentacaoService.listarTodasDTO();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/buscarPorId/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<MovimentacaoDTO> buscarPorId(@PathVariable Long id) {
        return movimentacaoService.buscarPorMovimentacaoId(id)
                .map(movimentacaoService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filtrarPorTipo")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MovimentacaoDTO>> filtrarPorTipo(@RequestParam String tipo) {
        List<MovimentacaoDTO> lista = movimentacaoService.listarPorTipoMovimentacao(tipo).stream()
                .map(movimentacaoService::toDTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/filtrarPorData")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MovimentacaoDTO>> filtrarPorData(@RequestParam String dataInicio, @RequestParam String dataFim) {
        LocalDate inicio = LocalDate.parse(dataInicio);
        LocalDate fim = LocalDate.parse(dataFim);
        List<MovimentacaoDTO> lista = movimentacaoService.listarPorIntervaloDeDatas(inicio, fim).stream()
                .map(movimentacaoService::toDTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/filtrarPorProduto")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MovimentacaoDTO>> filtrarPorProduto(@RequestParam Long produtoId) {
        List<MovimentacaoDTO> lista = movimentacaoService.listarPorProdutoId(produtoId).stream()
                .map(movimentacaoService::toDTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/atualizarMovimentacao/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<MovimentacaoDTO> atualizarMovimentacao(@PathVariable Long id, @RequestBody MovimentacaoEntity dadosAtualizados) {
        try {
            MovimentacaoEntity atualizado = movimentacaoService.atualizar(id, dadosAtualizados);
            MovimentacaoDTO dto = movimentacaoService.toDTO(atualizado);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/deletarMovimentacao/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long id) {
        movimentacaoService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para importar movimentações via arquivo Excel
    @PostMapping("/importarMovimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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