## Variaveis de runtime

Configure estas variaveis simples na task definition do ECS:

```text
SPRING_PROFILES_ACTIVE=prod
JWT_EXPIRATION_MS=28800000
QUEUE_CORS_ALLOWED_ORIGIN_PATTERNS=https://
QUEUE_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS=https://
```

Configure estas variaveis como ECS secrets vindos do AWS Secrets Manager:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
QUEUE_ADMIN_USERNAME
QUEUE_ADMIN_PASSWORD
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
  },
  {
    "name": "QUEUE_ADMIN_USERNAME",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:QUEUE_ADMIN_USERNAME::"
  },
  {
    "name": "QUEUE_ADMIN_PASSWORD",
    "valueFrom": "arn:aws:secretsmanager:us-east-2:997985547557:secret:secrets-mysql-memPRP:QUEUE_ADMIN_PASSWORD::"
  }
]
```

O usuario admin inicial so e criado quando `QUEUE_ADMIN_PASSWORD` esta
configurado e ainda nao existe um usuario com `QUEUE_ADMIN_USERNAME`.

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

## GitHub Actions

O workflow `.github/workflows/backend-ci.yml` roda testes, gera o jar e valida a
imagem Docker em pushes e pull requests.

Para ativar deploy automatico no ECS ao fazer push para `main` ou `master`,
configure estas variaveis em `Settings > Secrets and variables > Actions >
Variables`:

```text
AWS_ROLE_TO_ASSUME=arn:aws:iam::997985547557:role/NOME_DA_ROLE_GITHUB_ACTIONS
ECR_REPOSITORY=NOME_DO_REPOSITORIO_ECR
ECS_CLUSTER=default
ECS_SERVICE=queue-backend-5bc4
```

Use OIDC com `AWS_ROLE_TO_ASSUME`; nao coloque access key da AWS no GitHub
Actions.

## Execucao local

Crie um arquivo local `.env` baseado no `.env.example`, preencha somente
credenciais locais e rode:

```bash
docker compose up -d
```
