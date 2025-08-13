package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.FornecedorEntity;
import br.edu.fatecgru.insight_forge.service.FornecedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    // 🔄 CREATE
    @PostMapping("/criarFornecedor")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<FornecedorEntity> criarFornecedor(@RequestBody FornecedorEntity fornecedor) {
        FornecedorEntity novoFornecedor = fornecedorService.salvarOuAtualizarFornecedor(fornecedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoFornecedor);
    }

    // 📄 READ - Listar todos
    @GetMapping("/listarFornecedores")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<FornecedorEntity>> listarFornecedores() {
        List<FornecedorEntity> fornecedores = fornecedorService.listarTodos();
        return ResponseEntity.ok(fornecedores);
    }

    // 🔍 READ - Buscar por ID
    @GetMapping("/buscarPorId/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<FornecedorEntity> buscarPorId(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✏️ UPDATE
    @PutMapping("/atualizarFornecedor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<FornecedorEntity> atualizarFornecedor(
            @PathVariable Long id,
            @RequestBody FornecedorEntity dadosAtualizados) {

        try {
            FornecedorEntity atualizado = fornecedorService.atualizarFornecedor(id, dadosAtualizados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ❌ DELETE
    @DeleteMapping("/deletarFornecedor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deletarFornecedor(@PathVariable Long id) {
        fornecedorService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }
}