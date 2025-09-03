package br.edu.fatecgru.insight_forge.config;

import br.edu.fatecgru.insight_forge.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner init(UsuarioService usuarioService) {
        return args -> {
            usuarioService.inicializarAdministradorPadrao();
        };
    }
}
