package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.MovimentacaoRepository;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoEntity salvarOuAtualizar(MovimentacaoEntity movimentacao) {
        return movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoEntity> listarTodas() {
        return movimentacaoRepository.findAll();
    }

    public Optional<MovimentacaoEntity> buscarPorMovimentacaoId(Long id) {
        return movimentacaoRepository.findById(id);
    }

    public List<MovimentacaoEntity> listarPorProdutoId(Long produtoId) {
        return movimentacaoRepository.findByProdutoId(produtoId);
    }

    public List<MovimentacaoEntity> listarPorTipoMovimentacao(String tipoMovimentacao) {
        return movimentacaoRepository.findByTipoMovimentacao(tipoMovimentacao);
    }

    public List<MovimentacaoEntity> listarPorIntervaloDeDatas(LocalDate dataInicio, LocalDate dataFim) {
        return movimentacaoRepository.findByDataMovimentacaoBetween(dataInicio, dataFim);
    }

    public void deletarPorId(Long id) {
        movimentacaoRepository.deleteById(id);
    }

    public MovimentacaoEntity atualizar(Long id, MovimentacaoEntity dadosAtualizados) {
        return movimentacaoRepository.findById(id).map(movimentacao -> {
            movimentacao.setProduto(dadosAtualizados.getProduto());
            movimentacao.setQuantidadeMovimentada(dadosAtualizados.getQuantidadeMovimentada());
            movimentacao.setDataMovimentacao(dadosAtualizados.getDataMovimentacao());
            movimentacao.setTipoMovimentacao(dadosAtualizados.getTipoMovimentacao());
            return movimentacaoRepository.save(movimentacao);
        }).orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
    }

    @Async // Roda em uma thread separada
    @Transactional // Garante que a operação de importação seja transacional (tudo ou nada)
    public void importarMovimentacoesAsync(File file) {
        try {
            importarMovimentacoes(file);
        } finally {
            file.delete();
        }
    }

    @Transactional
    public void importarMovimentacoes(File file) {
        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nomeProduto = row.getCell(0).getStringCellValue();
                int quantidadeMovimentada = (int) row.getCell(1).getNumericCellValue();
                LocalDate dataMovimentacao = row.getCell(2).getLocalDateTimeCellValue().toLocalDate();
                String tipoMovimentacao = row.getCell(3).getStringCellValue();

                List<ProdutoEntity> produtos = produtoRepository.findByNomeContainingIgnoreCase(nomeProduto);
                if (produtos.isEmpty()) continue;

                ProdutoEntity produto = produtos.get(0);
                MovimentacaoEntity movimentacao = new MovimentacaoEntity();
                movimentacao.setProduto(produto);
                movimentacao.setQuantidadeMovimentada(quantidadeMovimentada);
                movimentacao.setDataMovimentacao(dataMovimentacao);
                movimentacao.setTipoMovimentacao(tipoMovimentacao);

                movimentacaoRepository.save(movimentacao);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar movimentações: " + e.getMessage(), e);
        }
    }

    public byte[] exportarMovimentacoes() throws IOException {
        List<MovimentacaoEntity> movimentacoes = movimentacaoRepository.findAll();
        return gerarExcelMovimentacoes(movimentacoes);
    }

    public byte[] exportarMovimentacoesPorProduto(Long produtoId) throws IOException {
        List<MovimentacaoEntity> movimentacoes = movimentacaoRepository.findAll().stream()
                .filter(m -> m.getProduto().getId().equals(produtoId))
                .toList();
        return gerarExcelMovimentacoes(movimentacoes);
    }

    public byte[] exportarMovimentacoesPorCategoria(String categoria) throws IOException {
        List<MovimentacaoEntity> movimentacoes = movimentacaoRepository.findAll().stream()
                .filter(m -> m.getProduto().getCategoria().equalsIgnoreCase(categoria))
                .toList();
        return gerarExcelMovimentacoes(movimentacoes);
    }

    private byte[] gerarExcelMovimentacoes(List<MovimentacaoEntity> movimentacoes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Movimentacoes");
            String[] headers = {"ID", "Produto", "Categoria", "Quantidade", "Data", "Tipo"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (MovimentacaoEntity m : movimentacoes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getProduto().getNome());
                row.createCell(2).setCellValue(m.getProduto().getCategoria());
                row.createCell(3).setCellValue(m.getQuantidadeMovimentada());
                row.createCell(4).setCellValue(m.getDataMovimentacao().toString());
                row.createCell(5).setCellValue(m.getTipoMovimentacao());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}