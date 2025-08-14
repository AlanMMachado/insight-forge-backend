package br.edu.fatecgru.insight_forge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoDTO {
    private Long id;
    private String nome;
    private BigDecimal preco;
    private BigDecimal custo;
    private String categoria;
    private String descricao;
    private int quantidadeEstoque;
    private Boolean ativo;
}
