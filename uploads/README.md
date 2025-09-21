# Pasta de uploads para imagens de produtos

Esta pasta armazena as imagens dos produtos enviadas pelos usuários.

## Estrutura
- Imagens são nomeadas como: `produto_{id}_{timestamp}.jpg`
- Tipos permitidos: JPEG, PNG
- Tamanho máximo: 5MB por arquivo

## Manutenção
- Imagens antigas são automaticamente removidas quando produtos são atualizados ou deletados
- Para limpeza manual, remova arquivos que não estão referenciados no banco de dados