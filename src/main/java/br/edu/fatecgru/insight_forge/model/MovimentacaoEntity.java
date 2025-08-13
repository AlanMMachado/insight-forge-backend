package br.edu.fatecgru.insight_forge.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@Entity
@Table(name = "tb_movimentacao")
public class MovimentacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonBackReference
    private ProdutoEntity produto;

    @Column(name = "quantidade_movimentada", nullable = false)
    private int quantidadeMovimentada;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "tipo_movimentacao")
    private String tipoMovimentacao;
}