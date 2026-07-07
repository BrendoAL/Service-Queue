# Sistema de Fila de Atendimento

Projeto full stack para gerenciamento de senhas de atendimento.

## Estrutura

- `backend/`: API Spring Boot, Java 25, Lombok, segurança JWT, WebSocket, Flyway e MySQL.
- `.github/workflows/`: pipeline de CI/CD do backend.
- `docker-compose.yml`: ambiente local com MySQL e backend.

## Fluxo principal

Totem gera senha, painel público recebe atualização via WebSocket, atendente chama/finaliza senha e admin consulta métricas.

## Comandos

```bash
./gradlew :backend:test
./gradlew :backend:bootJar
docker compose up -d
```

## Configuracao AWS

O backend usa variaveis de ambiente para banco e JWT. Veja
`docs/aws-ecs.md` para o mapeamento do ECS/Fargate com AWS Secrets Manager.
