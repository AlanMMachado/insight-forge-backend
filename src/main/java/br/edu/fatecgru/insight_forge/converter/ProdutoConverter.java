package br.edu.fatecgru.insight_forge.converter;

import br.edu.fatecgru.insight_forge.dto.ProdutoDTO;
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProdutoConverter {

    public ProdutoDTO toDTO(ProdutoEntity entity) {
        if (entity == null) return null;

        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setPreco(entity.getPreco());
        dto.setCategoria(entity.getCategoria());
        dto.setDescricao(entity.getDescricao());
        dto.setQuantidadeEstoque(entity.getQuantidadeEstoque());
        dto.setAtivo(entity.getAtivo());
        return dto;
    }

    public List<ProdutoDTO> toDTOList(List<ProdutoEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProdutoEntity toEntity(ProdutoDTO dto) {
        if (dto == null) return null;

        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(dto.getId());
        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());
        entity.setCategoria(dto.getCategoria());
        entity.setDescricao(dto.getDescricao());
        entity.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        entity.setAtivo(dto.getAtivo());
        return entity;
    }
}
