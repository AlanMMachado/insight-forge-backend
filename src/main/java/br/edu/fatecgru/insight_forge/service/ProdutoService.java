package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.converter.ProdutoConverter;
import br.edu.fatecgru.insight_forge.dto.ProdutoDTO;
import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoConverter produtoConverter;

    public ProdutoEntity salvarOuAtualizarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    public List<ProdutoEntity> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    public List<ProdutoDTO> listarTodosProdutosDTO() {
        return produtoConverter.toDTOList(produtoRepository.findAll());
    }

    public List<ProdutoEntity> buscarProdutosPorCategoria(String categoria) {
        return produtoRepository.findByCategoria(categoria);
    }

    public List<ProdutoDTO> buscarProdutosPorCategoriaDTO(String categoria) {
        return produtoConverter.toDTOList(produtoRepository.findByCategoria(categoria));
    }

    public List<ProdutoEntity> buscarProdutosAtivos(Boolean ativo) {
        return produtoRepository.findByAtivo(ativo);
    }

    public List<ProdutoDTO> buscarProdutosAtivosDTO(Boolean ativo) {
        return produtoConverter.toDTOList(produtoRepository.findByAtivo(ativo));
    }

    public List<ProdutoEntity> buscarProdutosPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<ProdutoDTO> buscarProdutosPorNomeDTO(String nome) {
        return produtoConverter.toDTOList(produtoRepository.findByNomeContainingIgnoreCase(nome));
    }

    public Optional<ProdutoEntity> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Optional<ProdutoDTO> buscarProdutoPorIdDTO(Long id) {
        return produtoRepository.findById(id).map(produtoConverter::toDTO);
    }

    public List<String> listarCategorias() {
        return produtoRepository.findDistinctCategorias();
    }

    // Recebe um arquivo temporário do endpoint de "importarProdutos", chama a função de importação síncrona, logo em seguida apaga o arquivo temporário
    @Async // Roda em uma thread separada
    @Transactional // Garante que a operação de importação seja transacional (tudo ou nada)
    public void importarProdutosAsync(File file) {
        try{
            importarProdutos(file); // Chama a função síncrona de importação
        } finally {
            file.delete(); // Deleta o arquivo temporário após a importação
        }
    }

    // Função síncrona para importar produtos de um arquivo Excel
    @Transactional
    public void importarProdutos(File file) {
        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ProdutoEntity produto = new ProdutoEntity();
                produto.setNome(getCellValueAsString(row.getCell(0)));
                produto.setPreco(getCellValueAsBigDecimal(row.getCell(1)));
                produto.setCusto(getCellValueAsBigDecimal(row.getCell(2)));
                produto.setDescricao(getCellValueAsString(row.getCell(3)));
                produto.setCategoria(getCellValueAsString(row.getCell(4)));
                produto.setQuantidadeEstoque(getCellValueAsInteger(row.getCell(5)));

                produtoRepository.save(produto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar produtos: " + e.getMessage(), e);
        }
    }

    // Exportação de produtos para planilha Excel
    public byte[] exportarProdutos() throws IOException {
        List<ProdutoEntity> produtos = produtoRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Produtos");
            String[] headers = {"Nome", "Preço", "Custo", "Descrição", "Categoria", "Quantidade Estoque", "Ativo"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (ProdutoEntity produto : produtos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(produto.getNome());
                row.createCell(1).setCellValue(produto.getPreco() != null ? produto.getPreco().doubleValue() : 0.0);
                row.createCell(2).setCellValue(produto.getCusto() != null ? produto.getCusto().doubleValue() : 0.0);
                row.createCell(3).setCellValue(produto.getDescricao());
                row.createCell(4).setCellValue(produto.getCategoria());
                row.createCell(5).setCellValue(produto.getQuantidadeEstoque());
                row.createCell(6).setCellValue(produto.getAtivo());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void deletarProdutoPorId(Long id) {
        produtoRepository.deleteById(id);
    }

    public ProdutoEntity atualizarProduto(Long id, ProdutoEntity produtoAtualizado) {
        return produtoRepository.findById(id).map(produto -> {
            produto.setNome(produtoAtualizado.getNome());
            produto.setDescricao(produtoAtualizado.getDescricao());
            produto.setCategoria(produtoAtualizado.getCategoria());
            produto.setPreco(produtoAtualizado.getPreco());
            produto.setCusto(produtoAtualizado.getCusto());
            produto.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
            produto.setFornecedor(produtoAtualizado.getFornecedor());
            produto.setAtivo(produtoAtualizado.getAtivo());
            return produtoRepository.save(produto);
        }).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }


    // Métodos auxiliares para obter valores de células do Excel
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
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

}