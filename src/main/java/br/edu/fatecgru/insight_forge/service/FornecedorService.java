package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.FornecedorEntity;
import br.edu.fatecgru.insight_forge.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FornecedorService {


    private final FornecedorRepository fornecedorRepository;

    // 🔄 CREATE ou UPDATE
    public FornecedorEntity salvarOuAtualizarFornecedor(FornecedorEntity fornecedor) {
        return fornecedorRepository.save(fornecedor);
    }

    // 📄 READ - Listar todos os fornecedores
    public List<FornecedorEntity> listarTodos() {
        return fornecedorRepository.findAll();
    }

    // 🔍 READ - Buscar fornecedor por ID
    public Optional<FornecedorEntity> buscarPorId(Long id) {
        return fornecedorRepository.findById(id);
    }

    // ✏️ UPDATE - Atualizar fornecedor existente
    public FornecedorEntity atualizarFornecedor(Long id, FornecedorEntity dadosAtualizados) {
        return fornecedorRepository.findById(id).map(fornecedor -> {
            fornecedor.setNome(dadosAtualizados.getNome());
            fornecedor.setCnpj(dadosAtualizados.getCnpj());
            fornecedor.setTelefone(dadosAtualizados.getTelefone());
            fornecedor.setDataCadastro(dadosAtualizados.getDataCadastro());
            return fornecedorRepository.save(fornecedor);
        }).orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com ID: " + id));
    }

    // ❌ DELETE - Remover fornecedor por ID
    public void deletarPorId(Long id) {
        fornecedorRepository.deleteById(id);
    }
}