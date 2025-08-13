package br.edu.fatecgru.insight_forge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovimentacaoDTO {
    private Long id;
    private ProdutoDTO produto;
    private int quantidadeMovimentada;
    private LocalDate dataMovimentacao;
    private String tipoMovimentacao;

}
