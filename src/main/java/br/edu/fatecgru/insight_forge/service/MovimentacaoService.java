package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.repository.MovimentacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;

    public MovimentacaoEntity salvarOuAtualizar(MovimentacaoEntity movimentacao) {
        return movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoEntity> listarTodas() {
        return movimentacaoRepository.findAll();
    }

    public Optional<MovimentacaoEntity> buscarPorId(Long id) {
        return movimentacaoRepository.findById(id);
    }

    public void deletarPorId(Long id) {
        movimentacaoRepository.deleteById(id);
    }

    public MovimentacaoEntity atualizar(Long id, MovimentacaoEntity dadosAtualizados) {
        return movimentacaoRepository.findById(id).map(movimentacao -> {
            movimentacao.setProduto(dadosAtualizados.getProduto());
            movimentacao.setEstoque(dadosAtualizados.getEstoque());
            movimentacao.setDataEntrada(dadosAtualizados.getDataEntrada());
            movimentacao.setDataSaida(dadosAtualizados.getDataSaida());
            movimentacao.setTipoMovimentacao(dadosAtualizados.getTipoMovimentacao());
            return movimentacaoRepository.save(movimentacao);
        }).orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
    }
}