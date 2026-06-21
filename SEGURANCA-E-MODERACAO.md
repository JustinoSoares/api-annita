# Segurança, Moderação e Políticas — Annita

> Estrutura simples e escalável para prevenir conteúdo impróprio na plataforma.

> **📖 Documentação da API:** acesse `/swagger-ui.html` ou `/v3/api-docs` (Swagger/OpenAPI) para ver todos os endpoints documentados automaticamente.
>
> **Formato de paginação:** todas as listagens retornam `{ "data": [...], "meta": { "page", "perPage", "totalElements", "totalPages" } }` (page 0-indexed).

---

## 1. Sistema de Confiança do Utilizador

### Níveis de conta (progressivos)

| Nível | Requisitos | Permissões |
|---|---|---|
| **Visitante** | Nenhum | Ver eventos |
| **Participante** | E-mail confirmado | Pessoa interessada em participar ou receber notificações sobre eventos |
| **Contribuidor** | E-mail confirmado | Publicar sem limites eventos como hackathons, meetups, workshops, conferências, feiras, exposições e outros eventos de natureza formativa, cultural, recreativa, social, tecnológica, científica,desportiva ou de lazer |
| **Moderador** | Pessoa responsável pelo gerenciamento do sistema | Moderar denúncias, suspender/banir |
| **Admin** | Admin do sistema | Tudo acima + gerir moderadores + configurar regras |

### Verificação de e-mail (obrigatória)

- Implementar logo após o registo (já existe UI: `EmailVerificationModal`)
- Código de 6 dígitos com expiração de 15 min
- Reenvio a cada 60s (máx 3 tentativas)

---

## 2. Barreiras Antes da Publicação

### 2.1. Aprovação de eventos (backend)

Implementado no backend (`EventService`):

- **Usuários novos** (< 2 eventos aprovados): eventos criados ficam com status `PENDING` — precisam de aprovação de um ADMIN/MODERATOR
- **Usuários com ≥ 2 eventos aprovados**: eventos são publicados automaticamente com status `APPROVED`
- **Usuários com evento denunciado** (`REPORTED`): voltam a precisar de aprovação no próximo evento
- **Admin aprova** → `PENDING` → `APPROVED` + incrementa `approvedEventCount` do autor
- **Admin rejeita** → `PENDING` → `REJECTED`

**Endpoints:**
| Método | Path | Função |
|---|---|---|
| `PUT` | `/api/events/{id}/approve` | Aprovar evento pendente |
| `PUT` | `/api/events/{id}/reject` | Rejeitar evento pendente |
| `GET` | `/api/events/admin` | Listar todos os eventos (com filtro por status) |

### 2.2. Sistema de Categorias (backend)

Categorias são agrupadas por `groupName`. Exemplo: grupo `"Formação"` pode conter as categorias `"Palestras"`, `"Workshops"`, `"Bootcamp"`.

| Método | Path | Função | Acesso |
|---|---|---|---|
| `GET` | `/api/categories` | Listar categorias (paginado) | Qualquer autenticado |
| `GET` | `/api/categories/by-group?groupName=X` | Listar categorias de um grupo | Qualquer autenticado |
| `GET` | `/api/categories/{id}` | Detalhes de uma categoria | Qualquer autenticado |
| `POST` | `/api/categories` | Criar categoria | ADMIN / MODERATOR |
| `PUT` | `/api/categories/{id}` | Atualizar categoria | ADMIN / MODERATOR |
| `DELETE` | `/api/categories/{id}` | Eliminar categoria | ADMIN / MODERATOR |

### 2.3. Limite de publicações (planeamento)

```ts
// Configurável por nível
const LIMITES = {
  verificado: 3,  // eventos por semana
  contribuidor: Infinity,
};
```

### 2.4. Filtros automáticos no formulário

- **Palavras proibidas** — lista editável no backend (bloqueia título/descrição)
- **URLs maliciosos** — detectar domains encurtados ou blacklisted no campo URL
- **Duplicados** — verificar se título igual já foi publicado pelo mesmo autor nos últimos 30 dias
- **Imagens** — limite de 2 (já feito); verificar formato/tamanho (<5MB cada) no upload

### 2.5. "Compromisso de veracidade"

Já existe (`PublishConfirmationModal`). Deve ser guardado em log (quem + quando).

---

## 3. Sistema de Denúncias (Pós-Publicação)

### 3.1. Fluxo da denúncia (backend implementado)

**Endpoint:** `POST /api/events/{id}/report` (qualquer utilizador autenticado)
- Remove o evento da visualização pública (`status` → `REPORTED`)
- O autor do evento volta a precisar de aprovação nos próximos eventos
- O evento fica visível no painel de administração (`GET /api/events/admin?status=REPORTED`)

```
Interface (frontend):
Utilizador clica "Denunciar"
    → Modal com motivos (select obrigatório):
        [ ] Informação falsa/enganosa
        [ ] Evento fraudulento (burla)
        [ ] Conteúdo ofensivo/abusivo
        [ ] Spam ou repetido
        [ ] Categoria incorreta
        [ ] Outro (campo texto livre)
    → Opcional: descrição adicional
    → Submeter → POST /api/events/{id}/report
```

### 3.2. Estados do evento (backend)

| Estado | Descrição | Visível publicamente |
|---|---|---|
| `PENDING` | Aguardando aprovação do admin | ❌ |
| `APPROVED` | Publicado e visível | ✅ |
| `REJECTED` | Rejeitado pelo admin | ❌ |
| `REPORTED` | Denunciado, removido da exibição | ❌ |

**Ciclo de vida:**
```
Criação → PENDING ──(approve)──→ APPROVED ──(report)──→ REPORTED
                    └──(reject)──→ REJECTED
```

### 3.3. Lógica de moderação (planeamento)

| Acumulado de denúncias (mesmo evento, 7 dias) | Ação automática |
|---|---|
| 1-2 denúncias | Sinalizado para fila de moderação |
| 3+ denúncias | Evento removido temporariamente + notifica moderadores |
| 5+ denúncias (autor diferente) | Evento removido + autor suspenso automático |

### 3.4. Painel do moderador

```
/__mod  (rota protegida, apenas moderadores+)

Fila de Denúncias
┌────────────────────────────────────────────────────┐
│ [🔴] Hackathon Angola 2026   3 denúncias  │
│     🕐 há 2h  |  Informação falsa, Spam         │
│     [Aprovar denúncia]  [Ignorar]  [Ver evento]  │
├────────────────────────────────────────────────────┤
│ [🟡] Workshop React          1 denúncia           │
│     🕐 há 1d  |  Categoria incorreta             │
│     [Aprovar denúncia]  [Ignorar]  [Ver evento]  │
└────────────────────────────────────────────────────┘
```

**Ações do moderador:**
- **Aprovar denúncia** → remove evento + envia notificação ao autor
- **Ignorar** → arquiva (denunciante não é notificado)
- **Ver evento** → pré-visualização
- **Suspender autor** → ban temporário (1d, 7d, 30d, permanente)
- **Ações em lote** → selecionar múltiplas + aplicar ação

---

## 4. Políticas de Conteúdo e Sanções

### 4.1. O que é proibido (resumo para a plataforma)

1. **Eventos falsos ou fraudulentos** — cobrar por eventos que não existem
2. **Conteúdo ofensivo** — discurso de ódio, discriminação, assédio
3. **Spam** — eventos repetidos, links de afiliado não declarados
4. **Categoria errada** — intencionalmente enganosa para ganhar visibilidade
5. **Conteúdo ilegal** — qualquer violação da lei angolana

### 4.2. Sanções progressivas

| Infração | 1ª vez | 2ª vez | 3ª vez |
|---|---|---|---|
| Informação enganosa/levemente incorreta | Aviso + correção obrigatória | Evento removido + suspensão 7d | Ban permanente |
| Spam | Evento removido + suspensão 3d | Suspensão 30d | Ban permanente |
| Fraude/Burla | Ban permanente | — | — |
| Conteúdo ofensivo | Evento removido + suspensão 7d | Suspensão 30d | Ban permanente |

### 4.3. Termos de Uso e Política de Privacidade

Criar páginas estáticas:
- `/termos` — Termos de Uso (inclui regras de publicação, sanções, direitos da plataforma)
- `/privacidade` — Política de Privacidade (LGPD angolana / proteção de dados)

Referenciar no footer e no modal de publicação.

---

## 5. Segurança Técnica

### 5.1. Rate Limiting

| Endpoint | Limite | Janela |
|---|---|---|
| Publicar evento | 1 requisição | 30 segundos |
| Denunciar | 5 denúncias | 1 hora |
| Sign-in | 5 tentativas | 15 minutos |
| Sign-up | 3 contas | 24 horas (por IP) |

### 5.2. Sanitização de input

- Título e descrição: remover HTML/scripts antes de guardar
- URLs: validar formato, bloquear IPs internos, domains maliciosos
- Imagens: validar tipo real (não confiar no `Content-Type` do upload)

### 5.3. Headers de segurança (`next.config.ts`)

```ts
// next.config.ts
const nextConfig = {
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: [
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "X-Frame-Options", value: "DENY" },
          { key: "X-XSS-Protection", value: "1; mode=block" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
        ],
      },
    ];
  },
};
```

### 5.4. Proteção CSRF

- Usar tokens CSRF em todas as mutações (Next.js Server Actions já incluem proteção nativa)

---

## 6. Implementação Sugerida (Ordem Prioritária)

### Fase 1 — Essencial (MVP seguro)
1. Sistema de autenticação (NextAuth / lucia / Clerk)
2. Verificação de e-mail
3. Rate limiting nos endpoints
4. Sanitização de input
5. Termos de Uso + Política de Privacidade

### Fase 2 — Moderação básica
6. Sistema de denúncias com back-end
7. Painel de moderação simples
8. Limite semanal de publicações por nível

### Fase 3 — Maturidade
9. Filtros automáticos (palavras proibidas, duplicados)
10. Sanções automáticas por acumulado de denúncias
11. Dashboard de métricas para admins

---

## 7. Stack Recomendada (compatível com Next.js 16)

| Funcionalidade | Biblioteca/Solução |
|---|---|
| **Auth** | `lucia` (leve) ou `Clerk` (gerido) |
| **Database** | `PostgreSQL` + `Prisma` ou `Supabase` |
| **Rate limiting** | `upstash-rate-limiter` ou `express-rate-limit` (se API route) |
| **Validação** | `zod` (schemas de formulário e API) |
| **Sanitização** | `sanitize-html` ou `DOMPurify` |
| **Imagens** | `uploadthing` ou `Cloudinary` (com validação server-side) |

---

> **Nota:** Este documento é um guia prático — cada item deve ser implementado como uma tarefa separada. O foco inicial deve ser **autenticação + rate limiting + sanitização**, que cobrem >80% dos riscos comuns.
