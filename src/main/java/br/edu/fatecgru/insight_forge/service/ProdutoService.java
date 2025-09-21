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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoService.class);
    
    private final ProdutoRepository produtoRepository;
    private final ProdutoConverter produtoConverter;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.allowed-types}")
    private String allowedTypes;

    // ===============================
    // MÉTODOS RELACIONADOS AO USUÁRIO
    // ===============================

    public List<ProdutoEntity> listarProdutosPorUsuario(br.edu.fatecgru.insight_forge.model.UsuarioEntity usuario) {
        return produtoRepository.findByUsuario(usuario);
    }

    public ProdutoEntity salvarOuAtualizarProduto(ProdutoEntity produto) {
        return produtoRepository.save(produto);
    }

    public List<ProdutoDTO> buscarProdutosPorCategoriaDTO(String categoria) {
        return produtoConverter.toDTOList(produtoRepository.findByCategoria(categoria));
    }

    public List<ProdutoDTO> buscarProdutosAtivosDTO(Boolean ativo) {
        return produtoConverter.toDTOList(produtoRepository.findByAtivo(ativo));
    }

    public List<ProdutoDTO> buscarProdutosPorNomeDTO(String nome) {
        return produtoConverter.toDTOList(produtoRepository.findByNomeContainingIgnoreCase(nome));
    }

    public Optional<ProdutoDTO> buscarProdutoPorIdDTO(Long id) {
        return produtoRepository.findById(id).map(produtoConverter::toDTO);
    }

    public Optional<ProdutoEntity> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public List<String> listarCategorias() {
        return produtoRepository.findDistinctCategorias();
    }

    // =====================
    // MÉTODOS DE IMPORTAÇÃO
    // =====================

    @Transactional
    public List<String> importarProdutos(File file, br.edu.fatecgru.insight_forge.model.UsuarioEntity usuario) {
        List<String> produtosIgnorados = new java.util.ArrayList<>();
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
                produto.setUsuario(usuario);
                // Validação de duplicidade por nome + usuário
                List<ProdutoEntity> existentes = produtoRepository.findByNomeAndUsuario(produto.getNome(), usuario);
                if (!existentes.isEmpty()) {
                    produtosIgnorados.add(produto.getNome());
                    continue;
                }
                produtoRepository.save(produto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar produtos: " + e.getMessage(), e);
        }
        return produtosIgnorados;
    }

    @Async
    @Transactional
    public List<String> importarProdutosAsync(File file, br.edu.fatecgru.insight_forge.model.UsuarioEntity usuario) {
        try {
            return importarProdutos(file, usuario);
        } finally {
            file.delete();
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
        ProdutoEntity produto = buscarProdutoPorId(id).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        if (produto.getFotoUrl() != null) {
            try {
                Path filePath = Paths.get(uploadDir + produto.getFotoUrl().replace("/uploads/", ""));
                Files.deleteIfExists(filePath);
                logger.info("Foto deletada para produto {}: {}", id, produto.getFotoUrl());
            } catch (IOException e) {
                logger.error("Erro ao deletar foto do produto {}: {}", id, e.getMessage());
            }
        }
        produtoRepository.deleteById(id);
        logger.info("Produto deletado: {}", id);
    }

    @Transactional
    public ProdutoEntity atualizarProdutoComFoto(Long id, ProdutoEntity produtoAtualizado, MultipartFile file, boolean removerFoto) throws IOException {
        ProdutoEntity produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        // Atualizar campos normais
        produto.setNome(produtoAtualizado.getNome());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setCategoria(produtoAtualizado.getCategoria());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setCusto(produtoAtualizado.getCusto());
        produto.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
        produto.setFornecedor(produtoAtualizado.getFornecedor());
        produto.setAtivo(produtoAtualizado.getAtivo());

        // Lidar com foto
        if (removerFoto) {
            // Remover foto atual
            if (produto.getFotoUrl() != null) {
                Path filePath = Paths.get(uploadDir + produto.getFotoUrl().replace("/uploads/", ""));
                Files.deleteIfExists(filePath);
                logger.info("Foto removida para produto {}: {}", id, produto.getFotoUrl());
            }
            produto.setFotoUrl(null);
        } else if (file != null && !file.isEmpty()) {
            // Validar arquivo
            if (!isValidImageFile(file)) {
                throw new RuntimeException("Arquivo deve ser uma imagem válida (JPEG ou PNG)");
            }
            
            // Salvar nova foto, deletar antiga se existir
            if (produto.getFotoUrl() != null) {
                Path oldFilePath = Paths.get(uploadDir + produto.getFotoUrl().replace("/uploads/", ""));
                Files.deleteIfExists(oldFilePath);
                logger.info("Foto antiga removida para produto {}: {}", id, produto.getFotoUrl());
            }
            
            // Salvar nova
            String fileName = generateFileName(id, file.getOriginalFilename());
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            produto.setFotoUrl("/uploads/" + fileName);
            logger.info("Nova foto salva para produto {}: {}", id, fileName);
        }
        // Se não remover e não enviar file, mantém foto atual

        return produtoRepository.save(produto);
    }

    @Transactional
    public ProdutoEntity salvarComFoto(Long id, MultipartFile file) throws IOException {
        ProdutoEntity produto = buscarProdutoPorId(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        if (file != null && !file.isEmpty()) {
            // Validações
            if (!isValidImageFile(file)) {
                throw new RuntimeException("Arquivo deve ser uma imagem válida (JPEG ou PNG)");
            }
            
            // Salvar arquivo
            String fileName = generateFileName(id, file.getOriginalFilename());
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Atualizar produto
            produto.setFotoUrl("/uploads/" + fileName);
            logger.info("Foto salva para produto {}: {}", id, fileName);
            return produtoRepository.save(produto);
        }
        return produto;
    }

    // =====================
    // MÉTODOS AUXILIARES
    // =====================

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // Verificar tamanho do arquivo (máx 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return false;
        }
        
        // Verificar MIME type
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        String[] allowedTypesArray = allowedTypes.split(",");
        boolean validMimeType = false;
        for (String type : allowedTypesArray) {
            if (contentType.equals(type.trim())) {
                validMimeType = true;
                break;
            }
        }
        
        if (!validMimeType) {
            return false;
        }
        
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) < 2) {
                return false;
            }
            
            // JPEG: FF D8
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                return true;
            }
            
            // PNG: 89 50 4E 47
            if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && 
                header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                return true;
            }
            
        } catch (IOException e) {
            logger.error("Erro ao verificar assinatura do arquivo: {}", e.getMessage());
            return false;
        }
        
        return false;
    }
    
    private String generateFileName(Long produtoId, String originalFilename) {
        String extension = ".jpg";
        if (originalFilename != null) {
            String safeFilename = Paths.get(originalFilename).getFileName().toString();
            if (safeFilename.contains(".")) {
                String originalExt = safeFilename.substring(safeFilename.lastIndexOf(".")).toLowerCase();
                if (originalExt.equals(".png") || originalExt.equals(".jpg") || originalExt.equals(".jpeg")) {
                    extension = originalExt.equals(".jpeg") ? ".jpg" : originalExt;
                }
            }
        }
        
        return "produto_" + produtoId + "_" + System.currentTimeMillis() + extension;
    }

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
