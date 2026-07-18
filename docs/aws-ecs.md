## Variaveis de runtime

Configure estas variaveis simples na task definition do ECS:

```text
SPRING_PROFILES_ACTIVE=prod
JWT_EXPIRATION_MS=28800000
QUEUE_CORS_ALLOWED_ORIGIN_PATTERNS=https://URL_DO_FRONTEND
QUEUE_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS=https://URL_DO_FRONTEND
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

O valor do segredo precisa ser um JSON com chaves exatamente iguais as variaveis
acima. Exemplo de formato, sem usar estes valores literais:

```json
{
  "SPRING_DATASOURCE_URL": "jdbc:mysql://HOST:3306/queue_db",
  "SPRING_DATASOURCE_USERNAME": "admin",
  "SPRING_DATASOURCE_PASSWORD": "senha-do-banco",
  "JWT_SECRET": "secret-base64-com-32-bytes-ou-mais",
  "QUEUE_ADMIN_USERNAME": "admin",
  "QUEUE_ADMIN_PASSWORD": "senha-inicial-do-admin"
}
```

Se a chave estiver como `JWT Secret`, com espaco, ou se
`QUEUE_ADMIN_PASSWORD` nao existir mas estiver mapeado na task definition, a
task pode falhar antes da aplicacao iniciar.

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

Se a aplicacao usa MySQL, o health check do Actuator tambem fica `DOWN` quando
o backend nao consegue conectar no RDS. Nesse caso o ALB pode responder `503`
porque nao ha nenhum target saudavel.

## Checklist quando o ECS fica com 0 tasks rodando

1. Veja o motivo da parada da ultima task:

```bash
aws ecs list-tasks \
  --cluster default \
  --service-name queue-backend-5bc4 \
  --desired-status STOPPED \
  --region us-east-2

aws ecs describe-tasks \
  --cluster default \
  --tasks TASK_ARN \
  --region us-east-2
```

2. Veja os eventos recentes do servico:

```bash
aws ecs describe-services \
  --cluster default \
  --services queue-backend-5bc4 \
  --region us-east-2 \
  --query 'services[0].events[0:10].[createdAt,message]' \
  --output table
```

3. Veja os logs da aplicacao:

```bash
aws logs tail /ecs/queue-backend-prod \
  --region us-east-2 \
  --since 1h
```

4. Confirme os pontos que mais costumam derrubar essa task:

- A execution role do ECS consegue ler `secretsmanager:GetSecretValue`.
- O segredo `secrets-mysql` e JSON e tem todas as chaves mapeadas.
- A task esta em subnets com rota ate o RDS.
- O security group do RDS aceita entrada MySQL `3306` a partir do security group da task.
- O target group usa porta `8080` e health check `/actuator/health`.
- As origens reais do frontend estao em `QUEUE_CORS_ALLOWED_ORIGIN_PATTERNS` e `QUEUE_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`.

## Observabilidade com Prometheus e Grafana

O backend expoe metricas Prometheus em:

```text
/actuator/prometheus
```

Para evitar manter Prometheus/Grafana dentro do ECS, use:

- Amazon Managed Service for Prometheus para armazenar metricas.
- Amazon Managed Grafana para dashboards.
- AWS Distro for OpenTelemetry Collector como sidecar ou servico ECS para coletar
  `http://localhost:8080/actuator/prometheus`.

O arquivo `deploy/adot-collector-prometheus.yml` contem uma configuracao base do
collector. Suba esse conteudo em um parametro SSM ou injete como
`AOT_CONFIG_CONTENT` no container do ADOT.

A task role usada pelo ADOT precisa permitir escrita no workspace Prometheus:

```json
{
  "Effect": "Allow",
  "Action": [
    "aps:RemoteWrite"
  ],
  "Resource": "arn:aws:aps:us-east-2:997985547557:workspace/SEU_WORKSPACE_ID"
}
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
