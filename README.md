# Lexi WhatsApp Agent — PoC API

API de prova de conceito para recebimento não-bloqueante de mensagens WhatsApp com processamento assíncrono por IA (
Lexi), agente de negociação da plataforma Arbitralis.

## Tecnologias

- **Kotlin** + **Spring Boot 4 (WebFlux)**
- **Kotlin Coroutines** — concorrência não-bloqueante
- **Resilience4j** — Circuit Breaker para resiliência na chamada ao LLM
- **JUnit 5 + Mockito** — testes unitários e de integração
- **springdoc-openapi** — documentação Swagger

## Como instalar e rodar

**Pré-requisitos:** Java 21+

```bash
# Clonar o repositório
git clone https://github.com/KleyvissonMatias/api-poc-whatsapp-agent-lexi.git
cd api-poc-whatsapp-agent-lexi

# Rodar a aplicação
./gradlew bootRun

# Rodar os testes
./gradlew test
```

A API sobe em `http://localhost:8080` e a documentação Swagger fica disponível em:
`http://localhost:8080/swagger-ui.html`

## Endpoint e Payload

### `POST /api/v1/webhook/messages`

Recebe a mensagem do WhatsApp, enfileira o processamento e retorna **202 Accepted** imediatamente.

**Request:**

```json
{
  "sender_id": "5511999999999",
  "message": "Olá, gostaria de negociar minha dívida.",
  "metadata": {
    "source": "whatsapp",
    "version": "1"
  }
}
```

**Response 202 Accepted:**

```json
{
  "status": "queued",
  "message_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-05-29T22:00:00.000Z"
}
```

**Response 400 Bad Request** (sender_id ou message em branco):

```json
{
  "error": "InvalidWebhookRequestException",
  "message": "sender_id cannot be blank",
  "timestamp": "2025-05-29T22:00:00.000Z",
  "path": "/api/v1/webhook/messages"
}
```

## Arquitetura

O sistema implementa **Arquitetura Hexagonal (Ports & Adapters)** com o seguinte fluxo:

```
[WhatsApp] → POST /webhook → [WebhookController]
                                     ↓
                          [ProcessInboundMessageUseCase]
                                     ↓
                          [InMemoryMessageQueue] ← enqueue(jobId)
                                     ↓
                          [MessageProcessingWorker] ← dequeue (background coroutine)
                                     ↓
                          [ProcessMessageJobUseCase]
                                     ↓
                          [ResilientLlmAdapter] → [SimulatedLlmAdapter] (simula LLM)
                                     ↓
                          [WhatsAppOutboundService] → Outbound Call (log simulado)
```

**Etapas:**

1. **Receber (Sync):** Webhook aceita a mensagem, cria o aggregate e enfileira → retorna 202
2. **Processar (Async):** Worker coroutine consome da fila e aciona o LLM em background
3. **Responder (Outbound Call):** Serviço de saída simula o disparo da resposta ao usuário

## ADR — Architecture Decision Record

### Decisão 1: Fila em Memória (PoC) → RabbitMQ / SQS (Produção)

**Contexto:** A PoC usa `ConcurrentLinkedQueue` em memória para desacoplar recebimento do processamento.

**Produção:** Utilizaria **RabbitMQ** ou **AWS SQS** pelos seguintes motivos:

- **Durabilidade:** mensagens persistidas em disco sobrevivem a reinicializações
- **Escalabilidade horizontal:** múltiplas instâncias da aplicação podem consumir a mesma fila
- **Dead Letter Queue (DLQ):** mensagens que falham repetidamente são movidas automaticamente para uma DLQ, permitindo
  análise e reprocessamento manual
- **Visibilidade:** dashboards de filas facilitam monitoramento de volume e lag

**Trade-off da PoC:** Estado em memória é perdido em caso de restart. Aceitável para demonstração do fluxo.

### Decisão 2: Circuit Breaker com Resilience4j

**Contexto:** O LLM externo apresenta lentidão e falhas intermitentes.

**Solução:** `ResilientLlmAdapter` envolve qualquer `LlmPort` com um Circuit Breaker Resilience4j:

- **CLOSED → OPEN:** abre após 50% de falhas em janela de 10 chamadas (mínimo 5 chamadas)
- **OPEN:** rejeita chamadas imediatamente por 30 segundos (fail-fast), protegendo o sistema
- **HALF-OPEN:** permite 3 chamadas de teste para verificar recuperação do serviço

**Benefício:** Evita cascata de falhas e libera a fila de processar outros jobs enquanto o LLM está instável.

### Decisão 3: Kotlin Coroutines (não Threads)

**Contexto:** Processar múltiplos jobs de LLM (cada um com ~10s de latência) com threads bloqueantes seria ineficiente.

**Solução:** O `MessageProcessingWorker` roda em `CoroutineScope(Dispatchers.Default)` e cada `delay()` da simulação de
LLM não bloqueia threads do pool, permitindo alta concorrência com poucos recursos.

### O que foi deixado de fora (PoC)

- Autenticação e validação de assinatura do webhook da Meta
- Banco de dados persistente (PostgreSQL / DynamoDB)
- Retry automático com backoff exponencial
- Observabilidade (OpenTelemetry / traces distribuídos)
- Contrato real com a API do WhatsApp Business
- Sanitização/mascaramento de PII nos logs de produção (aqui apenas o conteúdo da mensagem é omitido dos logs)

---

> This is a challenge by [Coodesh](https://coodesh.com/)
