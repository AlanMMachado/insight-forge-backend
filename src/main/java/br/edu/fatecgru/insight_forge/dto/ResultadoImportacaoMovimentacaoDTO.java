package br.edu.fatecgru.insight_forge.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResultadoImportacaoMovimentacaoDTO {
    private int movimentacoesImportadas;
    private int movimentacoesIgnoradas;
    private List<ProdutoNaoEncontradoDTO> produtosNaoEncontrados;
    private String mensagem;

    @Data
    public static class ProdutoNaoEncontradoDTO {
        private String nomeProduto;
        private int linha;
        private int quantidadeMovimentada;
        private String dataMovimentacao;
        private String tipoMovimentacao;

        public ProdutoNaoEncontradoDTO(String nomeProduto, int linha, int quantidadeMovimentada, String dataMovimentacao, String tipoMovimentacao) {
            this.nomeProduto = nomeProduto;
            this.linha = linha;
            this.quantidadeMovimentada = quantidadeMovimentada;
            this.dataMovimentacao = dataMovimentacao;
            this.tipoMovimentacao = tipoMovimentacao;
        }
    }
}
