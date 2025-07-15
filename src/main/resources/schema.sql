-- Tabela de Fornecedores
CREATE TABLE tb_fornecedor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    data_cadastro DATE NOT NULL
);

-- Tabela de Produtos
CREATE TABLE tb_produto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de Estoque
CREATE TABLE tb_estoque (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    data_cadastro DATETIME NOT NULL,
    CONSTRAINT fk_estoque_produto FOREIGN KEY (produto_id) REFERENCES tb_produto(id)
);

-- Tabela de Movimentação
CREATE TABLE tb_movimentacao (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    produto_id BIGINT NOT NULL,
    estoque_id BIGINT NOT NULL,
    data_entrada DATE DEFAULT NULL,
    data_saida DATE DEFAULT NULL,
    tipo_movimentacao VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_movimentacao_produto FOREIGN KEY (produto_id) REFERENCES tb_produto(id),
    CONSTRAINT fk_movimentacao_estoque FOREIGN KEY (estoque_id) REFERENCES tb_estoque(id)
);

-- Tabela de relacionamento Produto-Fornecedor (ManyToMany)
CREATE TABLE produto_fornecedor (
    produto_id BIGINT NOT NULL,
    fornecedor_id BIGINT NOT NULL,
    PRIMARY KEY (produto_id, fornecedor_id),
    CONSTRAINT fk_pf_produto FOREIGN KEY (produto_id) REFERENCES tb_produto(id),
    CONSTRAINT fk_pf_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES tb_fornecedor(id)
);