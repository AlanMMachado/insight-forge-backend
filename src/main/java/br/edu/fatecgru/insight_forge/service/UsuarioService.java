package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.UsuarioEntity;
import br.edu.fatecgru.insight_forge.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Set<String> ROLES_VALIDOS = new HashSet<>(Set.of("USER", "ADMIN"));
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioEntity registrarUsuario(String nome, String email, String password, String role) {
        if (!ROLES_VALIDOS.contains(role)) {
            throw new IllegalArgumentException("O papel do usuário deve ser 'USER' ou 'ADMIN'.");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }
        UsuarioEntity user = new UsuarioEntity();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return usuarioRepository.save(user);
    }

    public Optional<UsuarioEntity> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<UsuarioEntity> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<UsuarioEntity> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioEntity> atualizarUsuario(Long id, UsuarioEntity usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setEmail(usuarioAtualizado.getEmail());
            usuario.setRole(usuarioAtualizado.getRole());
            return usuarioRepository.save(usuario);
        });
    }

    public boolean excluirUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @PostConstruct
    public void inicializarAdministradorPadrao() {
        logger.info("Inicializando administrador padrão...");
        if (usuarioRepository.findByEmail("admin@insight.com").isEmpty()) {
            UsuarioEntity admin = new UsuarioEntity();
            admin.setNome("Administrador");
            admin.setEmail("admin@insight.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            usuarioRepository.save(admin);
            logger.info("Administrador padrão criado com sucesso.");
        } else {
            logger.info("Administrador padrão já existe.");
        }
    }
}
