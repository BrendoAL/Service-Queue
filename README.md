# Sistema de Fila de Atendimento

Projeto full stack para gerenciamento de senhas de atendimento.

## Estrutura

- `backend/`: API Spring Boot, Java 25, Lombok, segurança JWT, Kafka, WebSocket, Flyway e MySQL.
- `.github/workflows/`: pipeline de CI/CD do backend.
- `docker-compose.yml`: ambiente local com MySQL, Kafka e backend.

## Fluxo principal

Totem gera senha, backend publica evento Kafka, painel público recebe atualização via WebSocket, atendente chama/finaliza senha e admin consulta métricas.

## Comandos

```bash
./gradlew :backend:test
./gradlew :backend:bootJar
docker compose up -d
```
