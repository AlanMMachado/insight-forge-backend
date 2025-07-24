package br.edu.fatecgru.insight_forge.service;

import br.edu.fatecgru.insight_forge.model.ProdutoEntity;
import br.edu.fatecgru.insight_forge.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoEntity salvarOuAtualizarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    public List<ProdutoEntity> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    public List<ProdutoEntity> buscarProdutosPorCategoria(String categoria) {
        return produtoRepository.findByCategoria(categoria);
    }

    public List<ProdutoEntity> buscarProdutosAtivos(Boolean ativo) {
        return produtoRepository.findByAtivo(ativo);
    }

    public List<ProdutoEntity> buscarProdutosPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Optional<ProdutoEntity> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
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
                produto.setNome(row.getCell(0).getStringCellValue());
                produto.setPreco(BigDecimal.valueOf(row.getCell(1).getNumericCellValue()));
                produto.setDescricao(row.getCell(2).getStringCellValue());
                produto.setCategoria(row.getCell(3).getStringCellValue());
                produto.setQuantidadeEstoque((int) row.getCell(4).getNumericCellValue());

                // Adapte para fornecedores se necessário
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
            String[] headers = {"Nome", "Descrição", "Preço", "Categoria", "Ativo"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (ProdutoEntity produto : produtos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(produto.getNome());
                row.createCell(1).setCellValue(produto.getDescricao());
                row.createCell(2).setCellValue(produto.getPreco().doubleValue());
                row.createCell(3).setCellValue(produto.getCategoria());
                row.createCell(4).setCellValue(produto.getAtivo());
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
            produto.setFornecedores(produtoAtualizado.getFornecedores());
            produto.setAtivo(produtoAtualizado.getAtivo());
            return produtoRepository.save(produto);
        }).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }
}