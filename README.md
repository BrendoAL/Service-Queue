# Sistema de Fila de Atendimento

Projeto full stack para gerenciamento de senhas de atendimento. O fluxo cobre
totem, painel público, operação de atendente, administração de guichês/usuários,
relatórios e atualização em tempo real por WebSocket.

## Estrutura

- `backend/`: API Spring Boot, Java 25, Spring Security, JWT, WebSocket, Flyway e MySQL.
- `service-queue-frontend/`: frontend Angular servido em desenvolvimento pelo Angular CLI e em produção por nginx.
- `deploy/`: exemplos de infraestrutura para ECS/Fargate e observabilidade.
- `docs/aws-ecs.md`: guia de variáveis, secrets, health check e investigação de falhas no ECS.
- `docker-compose.yml`: ambiente local com MySQL e backend.

## Requisitos

- Java 25.
- Docker e Docker Compose.
- Node.js compatível com Angular 21 e npm 10 ou superior para o frontend.
- MySQL 8, quando não estiver usando o `docker-compose.yml`.

## Backend Local

Crie um `.env` local a partir de `.env.example` e preencha apenas valores locais.
O `JWT_SECRET` precisa ser Base64 com pelo menos 32 bytes.

```bash
openssl rand -base64 32
docker compose up -d
```

Comandos úteis do backend:

```bash
./gradlew :backend:test
./gradlew :backend:bootJar
```

Endpoints públicos do backend:

- `GET /actuator/health`
- `GET /actuator/prometheus`
- `POST /api/tickets`
- `GET /api/queue/status`
- `POST /api/auth/login`

## Frontend Local

```bash
cd service-queue-frontend
npm install
npm start
```

Abra `http://localhost:4200/`.

O frontend lê a URL padrão do backend em
`service-queue-frontend/public/assets/runtime-config.json`. O usuário também
pode editar a URL no cabeçalho da tela; esse valor fica salvo no `localStorage`
como `queue-api-base`.

## Docker Do Frontend

```bash
cd service-queue-frontend
docker build -t service-queue-frontend .
docker run --rm -p 8081:80 service-queue-frontend
```

Para apontar a mesma imagem para outro backend sem rebuild:

```bash
docker run --rm -p 8081:80 \
  -e QUEUE_API_BASE_URL=https://backend.example.com \
  service-queue-frontend
```

## Configuração AWS

O backend em produção usa `SPRING_PROFILES_ACTIVE=prod`, banco MySQL no RDS,
Secrets Manager e health check em `/actuator/health`. Veja
`docs/aws-ecs.md` para o mapeamento completo da task definition.

Variáveis simples esperadas na task do backend:

```text
SPRING_PROFILES_ACTIVE=prod
JWT_EXPIRATION_MS=28800000
QUEUE_CORS_ALLOWED_ORIGIN_PATTERNS=https://URL_DO_FRONTEND
QUEUE_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS=https://URL_DO_FRONTEND
```

Secrets esperados:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
QUEUE_ADMIN_USERNAME
QUEUE_ADMIN_PASSWORD
```

Não versionar senhas, access keys, connection strings reais ou JWT secrets.

## Investigação Rápida No ECS

Se a URL pública do backend responder `503 awselb/2.0`, o ALB está ativo, mas
sem target saudável. Comece por estes comandos:

```bash
aws ecs describe-services \
  --cluster default \
  --services queue-backend-5bc4 \
  --region us-east-2 \
  --query 'services[0].events[0:10].[createdAt,message]' \
  --output table

aws logs tail /ecs/queue-backend-prod \
  --region us-east-2 \
  --since 1h
```

Pontos mais comuns:

- Secrets Manager sem JSON com chaves exatamente iguais aos nomes mapeados.
- Execution role sem permissão `secretsmanager:GetSecretValue`.
- Security group do RDS sem entrada MySQL `3306` a partir do security group da task.
- Task em subnet sem rota adequada até o RDS.
- Target group apontando para porta diferente de `8080` ou health check diferente de `/actuator/health`.
- CORS/WebSocket sem a origem pública real do frontend.

## WebSocket

O backend expõe STOMP em `/ws`.

- `/topic/queue/status`: resumo público da fila.
- `/topic/tickets`: eventos autenticados de tickets.

Eventos de ticket: `CREATED`, `CALLED`, `RECALLED`, `STARTED`, `TRANSFERRED`,
`CANCELLED` e `COMPLETED`.

## Relatórios

Endpoints administrativos:

- `GET /api/reports/summary?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/reports/daily?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/reports/counters?from=YYYY-MM-DD&to=YYYY-MM-DD`
