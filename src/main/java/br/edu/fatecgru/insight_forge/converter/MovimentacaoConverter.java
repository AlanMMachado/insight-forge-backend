package br.edu.fatecgru.insight_forge.converter;

import br.edu.fatecgru.insight_forge.dto.MovimentacaoDTO;
import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MovimentacaoConverter {

    @Autowired
    private ProdutoConverter produtoConverter;

    public MovimentacaoDTO toDTO(MovimentacaoEntity entity) {
        if (entity == null) return null;

        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setId(entity.getId());
        dto.setQuantidadeMovimentada(entity.getQuantidadeMovimentada());
        dto.setDataMovimentacao(entity.getDataMovimentacao());
        dto.setTipoMovimentacao(entity.getTipoMovimentacao());
        dto.setProduto(produtoConverter.toDTO(entity.getProduto()));
        return dto;
    }

    public List<MovimentacaoDTO> toDTOList(List<MovimentacaoEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
