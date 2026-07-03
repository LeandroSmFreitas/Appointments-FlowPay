# FlowPay Appointments

Backend para o desafio tecnico da FlowPay, responsavel por rotear e distribuir atendimentos entre times de atendimento.

O projeto foi construido como um monolito modular com Spring Boot, PostgreSQL como fonte unica de verdade e fila logica persistida no banco. A solucao evita mensageria, Redis e microsservicos no MVP, mantendo simplicidade e evolucao gradual.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Bean Validation
- Springdoc OpenAPI / Swagger
- Server-Sent Events com `SseEmitter`
- Testcontainers para testes de integracao

## System Design

O desenho abaixo representa a visao de arquitetura e possiveis caminhos de evolucao. O backend atual implementa o nucleo como monolito modular, mantendo Redis, Kafka/RabbitMQ e separacao em servicos como alternativas futuras caso o volume real justifique.



## Dominio

A FlowPay possui tres times:

- `CARTOES`
- `EMPRESTIMOS`
- `OUTROS`

Regras principais:

- Cada agente atende no maximo 3 clientes simultaneamente.
- Se todos os agentes do time estiverem ocupados, o atendimento fica `WAITING`.
- Quando um atendimento e finalizado ou cancelado, o proximo `WAITING` do mesmo time e distribuido automaticamente.
- Quando um agente volta para `ONLINE`, ele tambem consome a fila do time ate preencher sua capacidade.
- A fila e logica e persistida no PostgreSQL.
- A distribuicao usa lock pessimista com `FOR UPDATE SKIP LOCKED`.

## Arquitetura Interna

Fluxo principal:

```text
HTTP
  -> Resource
  -> Facade
  -> Mapper
  -> Service
  -> Repository
  -> PostgreSQL
```

Responsabilidades:

- `web/rest`: endpoints REST.
- `facade`: orquestracao entre DTOs, mappers e services.
- `facade/dto`: contratos de entrada e saida.
- `facade/mapper`: mapeamento MapStruct.
- `service`: regras de negocio.
- `repository`: persistencia.
- `domain`: entidades JPA e enums.
- `exceptions`: tratamento padronizado de erros.
- `config`: OpenAPI, CORS e configuracoes web.

## Banco de Dados

As migrations ficam em:

```text
src/main/resources/db/migration
```

Tabelas principais:

- `teams`
- `agents`
- `attendances`

Todos os IDs sao `UUID`.

Times iniciais sao criados automaticamente via Flyway.

## Subindo PostgreSQL com Docker

O compose esta em:

```text
src/main/docker/docker-compose.yml
```

Subir o banco:

```powershell
docker compose -f src/main/docker/docker-compose.yml up -d
```

Parar o banco:

```powershell
docker compose -f src/main/docker/docker-compose.yml down
```

Configuracao padrao:

- Host: `localhost`
- Porta: `5432`
- Database: `appointments-flowpay`
- Usuario: `flowpay`
- Senha: `flowpay`

## Rodando a Aplicacao

Com o PostgreSQL em execucao:

```powershell
$env:JAVA_HOME='C:\Users\gangs\.jdks\ms-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

Variaveis de ambiente suportadas:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

## Swagger

Com a aplicacao rodando:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints

### Agents

```text
POST  /api/v1/agents
GET   /api/v1/agents
PATCH /api/v1/agents/{id}/status
GET   /api/v1/agents/{id}/attendances
```

### Attendances

```text
POST  /api/v1/attendances
GET   /api/v1/attendances
GET   /api/v1/attendances/{id}
PATCH /api/v1/attendances/{id}/finish
PATCH /api/v1/attendances/{id}/cancel
```

### Dashboard

```text
GET /api/v1/dashboard/summary
GET /api/v1/dashboard/events
```

## SSE

O endpoint de eventos em tempo real e:

```text
GET /api/v1/dashboard/events
Content-Type: text/event-stream
```

Eventos publicados:

- `ATTENDANCE_CREATED`
- `ATTENDANCE_ASSIGNED`
- `ATTENDANCE_FINISHED`
- `ATTENDANCE_CANCELLED`
- `AGENT_STATUS_CHANGED`

O SSE apenas avisa que algo mudou. O front deve buscar novamente:

```text
GET /api/v1/dashboard/summary
```

## Concorrencia

A distribuicao usa queries nativas com:

```sql
FOR UPDATE SKIP LOCKED
```

Isso evita que duas requisicoes concorrentes atribuam o mesmo atendimento ou excedam o limite de 3 atendimentos simultaneos por agente.

## Testes

Rodar testes:

```powershell
$env:JAVA_HOME='C:\Users\gangs\.jdks\ms-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd test
```

Observacao: os testes de integracao usam Testcontainers e exigem Docker ativo.

## Decisoes de Projeto

- Monolito modular para reduzir complexidade no MVP.
- PostgreSQL como unica fonte de verdade.
- Sem Kafka, RabbitMQ, Redis ou cache distribuido.
- Regras de negocio concentradas na camada `service`.
- Controllers sem regra de negocio.
- DTOs para todas as respostas publicas.
- Flyway para versionamento de schema.
- UUID em todos os identificadores.
