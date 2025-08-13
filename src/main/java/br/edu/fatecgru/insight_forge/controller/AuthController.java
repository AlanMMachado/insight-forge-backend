package br.edu.fatecgru.insight_forge.controller;

import br.edu.fatecgru.insight_forge.config.JwtUtil;
import br.edu.fatecgru.insight_forge.model.UsuarioEntity;
import br.edu.fatecgru.insight_forge.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Buscar o usuário completo para incluir todas as informações no JWT
            Optional<UsuarioEntity> userOptional = usuarioService.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuário não encontrado"));
            }

            UsuarioEntity user = userOptional.get();
            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", Map.of(
                            "id", user.getId(),
                            "name", user.getNome(),
                            "email", user.getEmail(),
                            "role", user.getRole()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Credenciais inválidas"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String nome = request.get("nome");
            String email = request.get("email");
            String password = request.get("password");
            String role = "USER"; // Role padrão para registro público

            UsuarioEntity user = usuarioService.registrarUsuario(nome, email, password, role);
            return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso", "usuario", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
