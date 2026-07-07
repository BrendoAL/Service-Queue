## Variaveis de runtime

Configure estas variaveis simples na task definition do ECS:

```text
SPRING_PROFILES_ACTIVE=prod
JWT_EXPIRATION_MS=28800000
```

Configure estas variaveis como ECS secrets vindos do AWS Secrets Manager:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
```

`JWT_SECRET` precisa ser Base64 e ter pelo menos 256 bits para HS256. Gere com:

```bash
openssl rand -base64 32
```

## Mapeamento do Secrets Manager

Se o segredo `secrets-mysql` guarda um objeto JSON com os mesmos nomes usados
pela aplicacao, mapeie cada chave JSON para a variavel de ambiente equivalente:

```json
[
  {
    "name": "SPRING_DATASOURCE_URL",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:SPRING_DATASOURCE_URL::"
  },
  {
    "name": "SPRING_DATASOURCE_USERNAME",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:SPRING_DATASOURCE_USERNAME::"
  },
  {
    "name": "SPRING_DATASOURCE_PASSWORD",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:SPRING_DATASOURCE_PASSWORD::"
  },
  {
    "name": "JWT_SECRET",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:JWT_SECRET::"
  }
]
```

O arquivo `deploy/ecs-task-definition.example.json` ja contem esse mapeamento
pronto para usar como base da task definition. Troque apenas a imagem ECR
`SEU_REPOSITORIO_ECR:latest` pelo nome real do repositorio/tag.

A task execution role precisa de permissao para ler o segredo:

```json
{
  "Effect": "Allow",
  "Action": [
    "secretsmanager:GetSecretValue"
  ],
  "Resource": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-*"
}
```

## Health check

Use este path no health check do target group:

```text
/actuator/health
```

## Execucao local

Crie um arquivo local `.env` baseado no `.env.example`, preencha somente
credenciais locais e rode:

```bash
docker compose up -d
```
