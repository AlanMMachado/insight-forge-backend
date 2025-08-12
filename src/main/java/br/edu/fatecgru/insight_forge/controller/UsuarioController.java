package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.model.UsuarioEntity;
import br.edu.fatecgru.insight_forge.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioEntity>> listarUsuarios() {
        List<UsuarioEntity> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("buscarUsuario/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioEntity> buscarUsuarioPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("atualizarUsuario/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioEntity> atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioEntity usuarioAtualizado) {
        return usuarioService.atualizarUsuario(id, usuarioAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("deletarUsuario/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        if (usuarioService.excluirUsuario(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/registrarAdmin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registrarAdmin(@RequestBody Map<String, String> request) {
        try {
            String nome = request.get("nome");
            String email = request.get("email");
            String password = request.get("password");
            String role = request.get("role");

            UsuarioEntity user = usuarioService.registrarUsuario(nome, email, password, role);
            return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso", "usuario", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registrarUser")
    public ResponseEntity<?> registrarUser(@RequestBody Map<String, String> request) {
        try {
            String nome = request.get("nome");
            String email = request.get("email");
            String password = request.get("password");
            String role = "USER"; // Role padrão para usuários comuns

            UsuarioEntity user = usuarioService.registrarUsuario(nome, email, password, role);
            return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso", "usuario", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
