package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.converter.MovimentacaoConverter;
import br.edu.fatecgru.insight_forge.dto.MovimentacaoDTO;
import br.edu.fatecgru.insight_forge.dto.ResultadoImportacaoMovimentacaoDTO;
import br.edu.fatecgru.insight_forge.model.MovimentacaoEntity;
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.MovimentacaoRepository;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoConverter movimentacaoConverter;

    public MovimentacaoEntity salvarOuAtualizar(MovimentacaoEntity movimentacao) {
        // Salvar a movimentação primeiro
        MovimentacaoEntity movimentacaoSalva = movimentacaoRepository.save(movimentacao);

        // Atualizar o estoque do produto baseado na movimentação
        atualizarEstoqueProduto(movimentacao);

        return movimentacaoSalva;
    }

    @Transactional
    public MovimentacaoEntity criarMovimentacao(MovimentacaoEntity movimentacao) {
        // Validar se o produto existe e buscar o produto completo
        ProdutoEntity produto = produtoRepository.findById(movimentacao.getProduto().getId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Associar o produto completo à movimentação
        movimentacao.setProduto(produto);

        // Validar estoque para vendas
        if ("VENDA".equalsIgnoreCase(movimentacao.getTipoMovimentacao())) {
            if (produto.getQuantidadeEstoque() < movimentacao.getQuantidadeMovimentada()) {
                throw new RuntimeException("Estoque insuficiente. Estoque atual: " +
                    produto.getQuantidadeEstoque() + ", Quantidade solicitada: " +
                    movimentacao.getQuantidadeMovimentada());
            }
        }

        // Salvar a movimentação
        MovimentacaoEntity movimentacaoSalva = movimentacaoRepository.save(movimentacao);

        // Atualizar o estoque do produto
        atualizarEstoqueProduto(movimentacaoSalva);

        return movimentacaoSalva;
    }

    @Transactional
    protected void atualizarEstoqueProduto(MovimentacaoEntity movimentacao) {
        ProdutoEntity produto = movimentacao.getProduto();
        int quantidadeMovimentada = movimentacao.getQuantidadeMovimentada();
        String tipoMovimentacao = movimentacao.getTipoMovimentacao();

        if ("COMPRA".equalsIgnoreCase(tipoMovimentacao)) {
            // Aumentar estoque para compras
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidadeMovimentada);
        } else if ("VENDA".equalsIgnoreCase(tipoMovimentacao)) {
            // Diminuir estoque para vendas
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidadeMovimentada);
        }

        // Salvar o produto com o estoque atualizado
        produtoRepository.save(produto);
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
    public ResultadoImportacaoMovimentacaoDTO importarMovimentacoes(File file) {
        List<ResultadoImportacaoMovimentacaoDTO.ProdutoNaoEncontradoDTO> produtosNaoEncontrados = new ArrayList<>();
        int movimentacoesImportadas = 0;
        int movimentacoesIgnoradas = 0;

        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nomeProduto = getCellValueAsString(row.getCell(0));
                int quantidadeMovimentada = getCellValueAsInteger(row.getCell(1));
                LocalDate dataMovimentacao = getCellValueAsDate(row.getCell(2));
                if (dataMovimentacao == null) {
                    String cellValue = row.getCell(2) != null ? row.getCell(2).toString() : "(célula vazia)";
                    throw new RuntimeException("Data de movimentação inválida ou ausente na linha " + (i + 1) + ". Valor encontrado: " + cellValue);
                }
                String tipoMovimentacao = getCellValueAsString(row.getCell(3));

                List<ProdutoEntity> produtos = produtoRepository.findByNomeContainingIgnoreCase(nomeProduto);
                if (produtos.isEmpty()) {
                    produtosNaoEncontrados.add(new ResultadoImportacaoMovimentacaoDTO.ProdutoNaoEncontradoDTO(
                        nomeProduto, i + 1, quantidadeMovimentada, dataMovimentacao.toString(), tipoMovimentacao
                    ));
                    movimentacoesIgnoradas++;
                    continue;
                }

                ProdutoEntity produto = produtos.get(0);
                MovimentacaoEntity movimentacao = new MovimentacaoEntity();
                movimentacao.setProduto(produto);
                movimentacao.setQuantidadeMovimentada(quantidadeMovimentada);
                movimentacao.setDataMovimentacao(dataMovimentacao);
                movimentacao.setTipoMovimentacao(tipoMovimentacao);

                // Usar o método criarMovimentacao para aplicar controle de estoque
                criarMovimentacao(movimentacao);
                movimentacoesImportadas++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar movimentações: " + e.getMessage(), e);
        }

        ResultadoImportacaoMovimentacaoDTO resultado = new ResultadoImportacaoMovimentacaoDTO();
        resultado.setMovimentacoesImportadas(movimentacoesImportadas);
        resultado.setMovimentacoesIgnoradas(movimentacoesIgnoradas);
        resultado.setProdutosNaoEncontrados(produtosNaoEncontrados);

        if (produtosNaoEncontrados.isEmpty()) {
            resultado.setMensagem("Todas as movimentações foram importadas com sucesso!");
        } else {
            resultado.setMensagem("Importação concluída com algumas movimentações ignoradas devido a produtos não encontrados.");
        }

        return resultado;
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

    public List<MovimentacaoDTO> listarTodasDTO() {
        return movimentacaoConverter.toDTOList(movimentacaoRepository.findAll());
    }

    public MovimentacaoDTO toDTO(MovimentacaoEntity entity) {
        return movimentacaoConverter.toDTO(entity);
    }

    // Métodos auxiliares para obter valores de células
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue());
            } catch (Exception e) {
                return null; // Retorna nulo se o formato não for válido
            }
        }
        return null;
    }

}
