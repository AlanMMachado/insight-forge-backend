# Insight Forge - Backend

Sistema de apoio à decisão com análise de dados e gestão de estoque, desenvolvido com Spring Boot, JPA e autenticação JWT.

## 📋 Sumário

- [Visão Geral](#visão-geral)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura do Projeto](#arquitetura-do-projeto)
- [Estrutura do Banco de Dados](#estrutura-do-banco-de-dados)
- [Configuração e Instalação](#configuração-e-instalação)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Endpoints da API](#endpoints-da-api)
- [Exemplos de Uso](#exemplos-de-uso)
- [Funcionalidades Especiais](#funcionalidades-especiais)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Troubleshooting](#troubleshooting)

## 🎯 Visão Geral

O **Insight Forge** é um sistema backend robusto para apoio à tomada de decisões empresariais, focado em:

- **Gestão de Produtos**: CRUD completo com categorização e controle de estoque
- **Controle de Fornecedores**: Cadastro e gestão de fornecedores
- **Movimentações de Estoque**: Registro automático de entradas e saídas com atualização do saldo
- **Autenticação Segura**: Sistema JWT com controle de roles (USER/ADMIN)
- **Importação/Exportação**: Suporte a arquivos Excel para operações em massa
- **Multi-tenant**: Dados isolados por usuário para segurança

## 🚀 Tecnologias Utilizadas

### Backend Framework
- **Java 21** - Linguagem principal
- **Spring Boot 3.5.3** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM

### Banco de Dados
- **MySQL 8+** - Banco de dados relacional
- **HikariCP** - Pool de conexões

### Bibliotecas Auxiliares
- **Lombok** - Redução de boilerplate
- **JJWT 0.11.5** - Manipulação de tokens JWT
- **Apache POI 5.4.0** - Manipulação de arquivos Excel
- **BCrypt** - Criptografia de senhas

### Build e Deploy
- **Maven** - Gerenciamento de dependências
- **Maven Wrapper** - mvnw incluído no projeto

## 🏗️ Arquitetura do Projeto

A aplicação segue uma arquitetura em camadas bem definida:

```
br.edu.fatecgru.insight_forge/
├── config/              # Configurações de segurança, CORS e JWT
├── controller/          # Controladores REST
├── converter/           # Conversores Entity ↔ DTO
├── dto/                # Objetos de transferência de dados
├── model/              # Entidades JPA
├── repository/         # Repositórios de dados
└── service/            # Regras de negócio
```

### Padrões Implementados
- **Repository Pattern** - Para abstração de acesso a dados
- **DTO Pattern** - Para transferência segura de dados
- **Service Layer** - Para lógica de negócio
- **Dependency Injection** - Gerenciado pelo Spring
- **JWT Token-based Authentication** - Autenticação stateless

## 🗄️ Estrutura do Banco de Dados

### Entidades Principais

#### UsuarioEntity (`tb_usuarios`)
```java
- id (Long, PK)
- nome (String, not null)
- email (String, unique, not null)
- password (String, BCrypt, not null)
- role (String: "USER" | "ADMIN")
- createdAt (LocalDateTime)
```

#### ProdutoEntity (`tb_produtos`)
```java
- id (Long, PK)
- nome (String, not null)
- preco (BigDecimal, not null)
- custo (BigDecimal)
- categoria (String)
- descricao (String)
- quantidadeEstoque (Integer)
- ativo (Boolean, default: true)
- fornecedor_id (FK → FornecedorEntity)
- usuario_id (FK → UsuarioEntity, not null)
```

#### MovimentacaoEntity (`tb_movimentacoes`)
```java
- id (Long, PK)
- produto_id (FK → ProdutoEntity, not null)
- usuario_id (FK → UsuarioEntity, not null)
- quantidadeMovimentada (Integer, not null)
- dataMovimentacao (LocalDate, not null)
- tipoMovimentacao (String: "COMPRA" | "VENDA")
```

#### FornecedorEntity (`tb_fornecedores`)
```java
- id (Long, PK)
- nome (String, not null)
- cnpj (String, unique)
- telefone (String)
- dataCadastro (LocalDate, not null)
```

### Relacionamentos
- **Usuario → Produtos**: One-to-Many (isolamento por usuário)
- **Usuario → Movimentacoes**: One-to-Many (isolamento por usuário)
- **Fornecedor → Produtos**: One-to-Many
- **Produto → Movimentacoes**: One-to-Many (histórico completo)

## ⚙️ Configuração e Instalação

### Pré-requisitos
- Java 21 ou superior
- MySQL 8.0 ou superior
- Maven 3.6+ (ou usar mvnw incluído)

### Configuração do Banco de Dados
1. Instalar e iniciar MySQL
2. Criar usuário/senha conforme `application.properties`
3. O banco `insightforge_db` será criado automaticamente

### Variáveis de Ambiente (Recomendado para Produção)
```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/insightforge_db
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

# JWT
JWT_SECRET=sua_chave_secreta_super_forte_aqui

# Profiles
SPRING_PROFILES_ACTIVE=prod
```

### Instalação

1. **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd insight-forge-backend
```

2. **Configure o banco de dados** (edite `src/main/resources/application.properties`)
```properties
spring.datasource.username=seu_usuario_mysql
spring.datasource.password=sua_senha_mysql
```

3. **Execute a aplicação**
```bash
# Opção 1: Usar Maven Wrapper (recomendado)
./mvnw spring-boot:run

# Opção 2: No Windows
mvnw.cmd spring-boot:run

# Opção 3: Build e execute o JAR
./mvnw clean package -DskipTests
java -jar target/insight-forge-0.0.1-SNAPSHOT.jar
```

4. **Acesso**
- API: `http://localhost:8080`
- Usuário padrão criado automaticamente:
  - Email: `admin@insight.com`
  - Senha: `admin123`
  - Role: `ADMIN`

## 🔐 Segurança e Autenticação

### Sistema JWT
- **Expiração**: 24 horas
- **Algoritmo**: HS256
- **Claims incluídos**: id, name, email, role
- **Header requerido**: `Authorization: Bearer <token>`

### Níveis de Acesso
- **Público**: `/api/auth/login`, `/api/auth/register`
- **USER/ADMIN**: `/api/produtos/**`, `/api/fornecedores/**`, `/api/movimentacoes/**`
- **Apenas ADMIN**: `/api/usuarios/**`

### CORS
Configurado para aceitar requisições de `http://localhost:3000` (frontend)

### Isolamento de Dados
Cada usuário só acessa seus próprios produtos e movimentações (multi-tenant por usuário).

## 📡 Endpoints da API

### 🔑 Autenticação (`/api/auth`)

#### POST `/api/auth/register`
Registro de novo usuário (role: USER)
```json
Request:
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "password": "senha123"
}

Response (200):
{
  "message": "Usuário registrado com sucesso",
  "usuario": { ... }
}
```

#### POST `/api/auth/login`
Autenticação e geração de token
```json
Request:
{
  "email": "admin@insight.com",
  "password": "admin123"
}

Response (200):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Administrador",
    "email": "admin@insight.com",
    "role": "ADMIN"
  }
}
```

### 👤 Usuários (`/api/usuarios`) - Apenas ADMIN

#### GET `/api/usuarios`
Lista todos os usuários

#### GET `/api/usuarios/buscarUsuario/{id}`
Busca usuário por ID

#### PUT `/api/usuarios/atualizarUsuario/{id}`
Atualiza dados do usuário

#### DELETE `/api/usuarios/deletarUsuario/{id}`
Remove usuário

#### POST `/api/usuarios/registrarAdmin`
Registra usuário com role específica
```json
Request:
{
  "nome": "Admin Novo",
  "email": "novo@admin.com",
  "password": "senha123",
  "role": "ADMIN"
}
```

### 📦 Produtos (`/api/produtos`) - USER/ADMIN

#### POST `/api/produtos/criarProduto`
```json
Request:
{
  "nome": "Notebook Dell",
  "preco": 2500.00,
  "custo": 2000.00,
  "categoria": "Eletrônicos",
  "descricao": "Notebook para trabalho",
  "quantidadeEstoque": 10,
  "ativo": true
}

Response (201): ProdutoDTO
```

#### GET `/api/produtos/listarProdutos`
Lista produtos do usuário autenticado

#### GET `/api/produtos/listarCategorias`
Lista todas as categorias distintas

#### GET `/api/produtos/buscarProdutoPorId/{id}`
Busca produto específico

#### GET `/api/produtos/buscarProdutoPorCategoria?categoria=Eletrônicos`
Filtra produtos por categoria

#### GET `/api/produtos/buscarProdutoPorNome?nome=Notebook`
Busca produtos por nome (LIKE)

#### GET `/api/produtos/buscarProdutosAtivos?ativo=true`
Filtra produtos ativos/inativos

#### PUT `/api/produtos/atualizarProduto/{id}`
Atualiza produto existente

#### DELETE `/api/produtos/deletarProduto/{id}`
Remove produto

#### POST `/api/produtos/importarProdutos`
Upload de arquivo Excel (multipart/form-data)
- Campo: `file`
- Formato: `.xlsx`
- Colunas: Nome, Preço, Custo, Descrição, Categoria, Quantidade

#### GET `/api/produtos/exportarProdutos`
Download de todos os produtos em Excel

### 🏢 Fornecedores (`/api/fornecedores`) - USER/ADMIN

#### POST `/api/fornecedores/criarFornecedor`
```json
Request:
{
  "nome": "Fornecedor ABC",
  "cnpj": "12.345.678/0001-90",
  "telefone": "(11) 98765-4321",
  "dataCadastro": "2024-01-15"
}
```

#### GET `/api/fornecedores/listarFornecedores`
Lista todos os fornecedores

#### GET `/api/fornecedores/buscarPorId/{id}`
Busca fornecedor específico

#### PUT `/api/fornecedores/atualizarFornecedor/{id}`
Atualiza fornecedor

#### DELETE `/api/fornecedores/deletarFornecedor/{id}`
Remove fornecedor

### 📊 Movimentações (`/api/movimentacoes`) - USER/ADMIN

#### POST `/api/movimentacoes/criarMovimentacao`
Registra movimentação (atualiza estoque automaticamente)
```json
Request:
{
  "produto": { "id": 1 },
  "quantidadeMovimentada": 5,
  "dataMovimentacao": "2024-01-15",
  "tipoMovimentacao": "VENDA"
}

Response (201): MovimentacaoDTO
```
> **Regras**: COMPRA aumenta estoque, VENDA diminui (valida disponibilidade)

#### GET `/api/movimentacoes/listarMovimentacoes`
Lista movimentações do usuário

#### GET `/api/movimentacoes/buscarPorId/{id}`
Busca movimentação específica

#### GET `/api/movimentacoes/filtrarPorTipo?tipo=VENDA`
Filtra por tipo de movimentação

#### GET `/api/movimentacoes/filtrarPorData?dataInicio=2024-01-01&dataFim=2024-01-31`
Filtra por período

#### GET `/api/movimentacoes/filtrarPorProduto?produtoId=1`
Filtra por produto

#### PUT `/api/movimentacoes/atualizarMovimentacao/{id}`
Atualiza movimentação (recalcula estoque)

#### DELETE `/api/movimentacoes/deletarMovimentacao/{id}`
Remove movimentação

#### POST `/api/movimentacoes/importarMovimentacoes`
Import via Excel
- Colunas: Nome do Produto, Quantidade, Data, Tipo

#### GET `/api/movimentacoes/exportar*`
Múltiplas opções de exportação:
- `/exportarMovimentacoes` - Todas
- `/exportarMovimentacoesPorProduto?produtoId=1`
- `/exportarMovimentacoesPorCategoria?categoria=Eletrônicos`
- `/exportarMovimentacoesPorData?dataInicio=...&dataFim=...`

### 🩺 Health (`/api/health`)
#### GET `/api/health`
Status da API (requer autenticação)

## 📝 Exemplos de Uso

### Fluxo Completo com cURL

1. **Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@insight.com","password":"admin123"}'
```

2. **Criar Produto** (usando token recebido)
```bash
curl -X POST http://localhost:8080/api/produtos/criarProduto \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Mouse Gamer","preco":150.00,"categoria":"Periféricos","quantidadeEstoque":20}'
```

3. **Registrar Venda**
```bash
curl -X POST http://localhost:8080/api/movimentacoes/criarMovimentacao \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"produto":{"id":1},"quantidadeMovimentada":2,"dataMovimentacao":"2024-01-15","tipoMovimentacao":"VENDA"}'
```

## ✨ Funcionalidades Especiais

### 🔄 Controle Automático de Estoque
- **COMPRA**: Incrementa `quantidadeEstoque`
- **VENDA**: Decrementa `quantidadeEstoque` (valida disponibilidade)
- **Atualização**: Recalcula ao editar/deletar movimentações

### 📊 Import/Export Excel
- **Importação**: Produtos e movimentações via planilha
- **Exportação**: Dados filtrados em diversos formatos
- **Validação**: Produtos duplicados são ignorados na importação

### 🔒 Isolamento Multi-tenant
- Cada usuário vê apenas seus próprios dados
- Produtos e movimentações isolados por `usuario_id`
- Fornecedores compartilhados entre usuários

### 🎯 DTOs e Conversores
- Exposição segura de dados (sem relações circulares)
- Conversão automática Entity ↔ DTO
- Responses padronizadas

## 📁 Estrutura de Pastas

```
src/
├── main/
│   ├── java/br/edu/fatecgru/insight_forge/
│   │   ├── config/
│   │   │   ├── DataInitializer.java        # Dados iniciais
│   │   │   ├── JwtAuthenticationFilter.java # Filtro JWT
│   │   │   ├── JwtUtil.java               # Utilitários JWT
│   │   │   ├── SecurityConfig.java        # Configuração Security
│   │   │   └── WebConfig.java            # Configuração CORS
│   │   ├── controller/
│   │   │   ├── AuthController.java        # Login/Register
│   │   │   ├── FornecedorController.java  # CRUD Fornecedores
│   │   │   ├── HealthController.java      # Health Check
│   │   │   ├── MovimentacaoController.java # CRUD Movimentações
│   │   │   ├── ProdutoController.java     # CRUD Produtos
│   │   │   └── UsuarioController.java     # Admin Usuários
│   │   ├── converter/
│   │   │   ├── MovimentacaoConverter.java
│   │   │   └── ProdutoConverter.java
│   │   ├── dto/
│   │   │   ├── MovimentacaoDTO.java
│   │   │   ├── ProdutoDTO.java
│   │   │   └── ResultadoImportacaoMovimentacaoDTO.java
│   │   ├── model/
│   │   │   ├── FornecedorEntity.java
│   │   │   ├── MovimentacaoEntity.java
│   │   │   ├── ProdutoEntity.java
│   │   │   └── UsuarioEntity.java
│   │   ├── repository/
│   │   │   ├── FornecedorRepository.java
│   │   │   ├── MovimentacaoRepository.java
│   │   │   ├── ProdutoRepository.java
│   │   │   └── UsuarioRepository.java
│   │   ├── service/
│   │   │   ├── FornecedorService.java
│   │   │   ├── MovimentacaoService.java
│   │   │   ├── ProdutoService.java
│   │   │   └── UsuarioService.java
│   │   └── InsightForgeApplication.java
│   └── resources/
│       ├── application.properties
│       └── application_example.properties
└── test/
    └── java/br/edu/fatecgru/insight_forge/
        └── InsightForgeApplicationTests.java
```

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. Erro de Conexão com Banco
```
Error: Communications link failure
```
**Solução**: Verificar se MySQL está rodando e credenciais estão corretas

#### 2. Token JWT Inválido
```
401 Unauthorized
```
**Soluções**:
- Verificar header `Authorization: Bearer <token>`
- Token pode ter expirado (24h)
- Refazer login para obter novo token

#### 3. Erro de Permissão
```
403 Forbidden
```
**Solução**: Verificar se usuário tem role adequada para o endpoint

#### 4. Estoque Insuficiente
```
400 Bad Request: "Estoque insuficiente"
```
**Solução**: Verificar quantidade disponível antes de registrar venda

#### 5. Produto Não Encontrado na Importação
**Solução**: Verificar se produtos existem antes de importar movimentações

### Logs Úteis

#### Ativar Debug SQL
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

#### Ativar Debug Security
```properties
logging.level.org.springframework.security=DEBUG
```

### Configurações de Desenvolvimento vs Produção

#### Desenvolvimento
```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

#### Produção
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
jwt.secret=${JWT_SECRET}
spring.datasource.url=${DB_URL}
```

---

**Desenvolvido por**: Alan Marques Machado