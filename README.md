# Sistema de Fila de Atendimento

Projeto full stack para gerenciamento de senhas de atendimento.

## Estrutura

- `backend/`: API Spring Boot, Java 25, Lombok, segurança JWT, WebSocket, Flyway e MySQL.
- `.github/workflows/`: pipeline de CI/CD do backend.
- `docker-compose.yml`: ambiente local com MySQL e backend.

## Fluxo principal

Totem gera senha, painel público recebe atualização via WebSocket, atendente chama/finaliza senha e admin consulta métricas.

## WebSocket

O backend expõe STOMP em `/ws`.

- Assine `/topic/tickets` para receber eventos `CREATED`, `CALLED`, `RECALLED` e `COMPLETED`.
- Assine `/topic/queue/status` para receber o resumo atualizado da fila após mudanças em senhas.

Configure as origens permitidas com `QUEUE_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`.

## Comandos

```bash
./gradlew :backend:test
./gradlew :backend:bootJar
docker compose up -d
```

## Configuracao AWS

O backend usa variaveis de ambiente para banco e JWT. Veja
`docs/aws-ecs.md` para o mapeamento do ECS/Fargate com AWS Secrets Manager.
