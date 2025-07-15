package br.edu.fatecgru.insight_forge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_movimentacao")
public class MovimentacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @ManyToOne
    @JoinColumn(name = "estoque_id", nullable = false)
    private EstoqueEntity estoque;

    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

    @Column(name = "data_saída")
    private LocalDate dataSaida;

    @Column(name = "tipo_movimetacao")
    private String tipoMovimentacao;
}
