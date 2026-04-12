# Alternativas para criar um gerador de projetos a partir do quarkus-ms-demo

## Contexto

O projeto `quarkus-ms-demo` é uma aplicação Quarkus single-module com 3 domínios de negócio (pet, store, user), stack de observabilidade, testes abrangentes, Docker, CI, etc. O objetivo é transformá-lo em um template reutilizável onde desenvolvedores possam informar parâmetros (nome do projeto, groupId, artifactId, pacote Java, etc.) e gerar um novo projeto personalizado.

### O que precisa ser parametrizado

- **Coordenadas Maven**: groupId (`org.acme`), artifactId (`quarkus-ms-demo`), version
- **Pacote Java**: `org.acme` → pacote customizado (afeta diretórios + conteúdo de arquivos)
- **Nome da aplicação**: em `application.properties`, `docker-compose.yml`, HTML template
- **Metadados OpenAPI**: título, descrição, versão
- **Docker/Infra**: nomes de containers, networks, image names
- **CI**: SonarCloud project key, repo URL
- **Seleção de domínios**: quais domínios incluir (pet, store, user) — nota: store depende de pet

---

## 8 Alternativas Analisadas

### 1. Copier (Python) — RECOMENDADO

**Como funciona**: Arquivo `copier.yml` declara variáveis e tipos. Projeto inteiro usa templates Jinja2 (`{{ variable }}`). Consumidor roda `copier copy gh:org/template ./meu-projeto`.

**Pontos fortes**:

- Sintaxe Jinja2 (`{{ }}`) **não conflita** com `${...}` do Maven/Quarkus — grande vantagem
- Prompts multi-choice nativos para seleção de domínios
- Prompts condicionais (`when`) — ex: perguntar sobre store só se pet foi selecionado
- Variáveis computadas — `package_path` derivado de `package_name` via `{{ package_name | replace('.', '/') }}`
- Exclusão condicional de diretórios/arquivos nativamente (`_exclude`)
- **Feature killer: `copier update`** — consumidores podem puxar mudanças do template (ex: upgrade de Quarkus) para projetos já gerados
- `pipx run copier` funciona sem instalação permanente

**Pontos fracos**: Requer Python/pipx (mas é leve)

---

### 2. Cookiecutter (Python) — SEGUNDA OPÇÃO

**Como funciona**: `cookiecutter.json` declara variáveis. Usa Jinja2. Consumidor roda `cookiecutter gh:org/template`.

**Pontos fortes**:

- Sintaxe Jinja2 sem conflito com Java/Maven
- Grande comunidade (36k+ stars no GitHub)
- Simples de configurar

**Pontos fracos**:

- Sem prompts condicionais ou multi-select nativos
- Inclusão condicional de diretórios requer hook `post_gen_project.py` (gera e depois deleta)
- Sem suporte a `update` (template é one-shot)

---

### 3. Maven Archetypes — OPÇÃO TRADICIONAL

**Como funciona**: Projeto archetype com templates Velocity. Diretórios usam `__packageInPathFormat__`. Consumidor roda `mvn archetype:generate`.

**Pontos fortes**:

- Nativo do ecossistema Java — familiar para devs Java
- Renomeação de pacotes é excelente (feature core)
- Pode ser bootstrapped com `mvn archetype:create-from-project`
- Sem dependência extra (só Maven)

**Pontos fracos**:

- **Conflito de sintaxe**: Velocity usa `${...}` igual ao Quarkus config — requer workarounds com `#set($dollar='$')`
- Manutenção alta — archetype é um projeto separado, sem link automático com o fonte
- Seleção condicional de domínios é muito difícil (não suporta inclusão condicional de diretórios)
- Templates Velocity são difíceis de ler/testar
- Mensagens de erro ruins durante geração
- Ecossistema em declínio

---

### 4. Hygen (Node.js) — BOA OPÇÃO SE HÁ NODE DISPONÍVEL

**Como funciona**: Templates EJS com frontmatter YAML ficam em `_templates/` dentro do próprio projeto. Consumidor roda `npx hygen project new` (sem instalação). O campo `to:` do frontmatter aceita variáveis para definir o caminho de saída de cada arquivo.

```text
_templates/
  project/
    new/
      pom.xml.t
      PetResource.java.t
      ...
    prompt.js         ← define os prompts interativos
```

**Pontos fortes**:

- `npx hygen` — **não requer instalação**, só Node.js/npm (que o time já tem)
- Sintaxe EJS (`<%= var %>`) **não conflita** com `${...}` do Maven/Quarkus
- Prompts via **Enquirer** — suporta multi-select nativo para seleção de domínios
- Templates **ficam dentro do próprio repo** — um único PR atualiza código e template
- Variáveis computadas via JavaScript no `prompt.js` (ex: `packagePath = packageName.replace(/\./g, '/')`)
- Inclusão condicional de arquivos via `to: <%= includeDomain ? 'path/file.java' : null %>`

**Pontos fracos**:

- Otimizado para scaffolding de componentes em projetos existentes, não para geração de projeto completo — funciona, mas não é o caso de uso principal
- Inclusão condicional é arquivo a arquivo — para ~60 arquivos Java com 3 domínios, o número de templates fica verboso
- **Sem `update`** — projetos já gerados não recebem mudanças do template automaticamente
- Dependência entre domínios (store requer pet) precisa ser validada manualmente no `prompt.js`

**Exemplo de template** (`_templates/project/new/PetResource.java.t`):

```yaml
---
to: src/main/java/<%= packagePath %>/pet/resources/PetResource.java
unless_exists: true
---
package <%= packageName %>.pet.resources;
// ...
```

**Exemplo de `prompt.js`**:

```js
module.exports = [
    { type: "input", name: "artifactId", message: "Artifact ID" },
    { type: "input", name: "packageName", message: "Package (ex: com.myco.myapp)" },
    { type: "multiselect", name: "domains", message: "Domínios", choices: ["pet", "store", "user"] },
    // packagePath é computado em seguida via after hook
];
```

---

### 5. Yeoman (Node.js) — MENÇÃO HONROSA

**Como funciona**: Generator em Node.js com templates EJS (`<%= var %>`). Consumidor roda `yo meu-generator`.

**Pontos fortes**:

- EJS não conflita com Java/Maven
- Flexibilidade total em prompts e lógica condicional
- Seleção de domínios excelente (checkbox prompts nativos)

**Pontos fracos**:

- Requer Node.js — fricção para devs Java
- **Ecossistema em declínio** (projeto em modo manutenção)
- Renomeação de pacotes/diretórios requer lógica JavaScript custom

---

### 5. GitHub Template Repositories — MUITO SIMPLES

**Como funciona**: Marcar repo como "Template" no GitHub. Users clicam "Use this template".

**Pontos fortes**: Zero setup, zero manutenção
**Pontos fracos**: Zero parametrização — user faz find-and-replace manual de tudo

**Veredicto**: Bom como complemento, não como solução principal.

---

### 6. Custom CLI (Picocli + Quarkus) — MÁXIMO CONTROLE

**Como funciona**: Aplicação CLI dedicada que lê templates e gera projetos.

**Pontos fortes**: Controle total, pode ser distribuído como native binary
**Pontos fracos**: Esforço alto — você mantém dois projetos. Overkill para este caso.

---

### 7. Quarkus CLI / code.quarkus.io — NÃO ADEQUADO

Gera esqueletos mínimos com extensões selecionadas. Não suporta templates customizados com código de negócio.

---

### 8. JHipster — NÃO ADEQUADO

Framework opinado para gerar projetos no estilo JHipster. Adaptar para seu projeto seria mais trabalho que qualquer outra opção.

---

## Tabela Comparativa

| Critério                | Hygen               | Copier       | Cookiecutter | Maven Archetype    | Yeoman          |
| ----------------------- | ------------------- | ------------ | ------------ | ------------------ | --------------- |
| Setup                   | Médio               | Médio        | Médio        | Médio              | Médio           |
| Flexibilidade de params | Bom                 | Excelente    | Bom          | Bom                | Excelente       |
| DX do consumidor        | Excelente (`npx`)   | Excelente    | Simples      | Familiar (Java)    | Bom             |
| Manutenção              | Baixa (embedded)    | Baixa-Média  | Média        | Alta               | Média           |
| Renomeação de pacotes   | Sim (custom JS)     | Sim (nativo) | Sim (nativo) | Excelente          | Sim (custom JS) |
| Seleção de domínios     | Médio (por arquivo) | Excelente    | Fraco-Médio  | Fraco              | Excelente       |
| Update de template      | Não                 | **SIM**      | Não          | Não                | Não             |
| Conflito sintaxe Java   | Nenhum              | Nenhum       | Nenhum       | **SIM** (`${...}`) | Nenhum          |
| Dependência extra       | Node.js             | Python/pipx  | Python       | Nenhuma            | Node.js         |
| Tendência ecosistema    | Ativo               | Crescendo    | Estável      | Declinando         | Declinando      |

---

## Ranking de Recomendação

**Se o time tem Node.js/npm (sem Python):**

1. **Hygen** — Melhor fit para o contexto. `npx hygen` sem instalação, templates embutidos no repo, sem conflito de sintaxe com Java/Maven. Limitação: inclusão condicional de domínios é verbosa (arquivo a arquivo).
2. **Maven Archetypes** — Alternativa puramente Java se Node.js também for um problema, mas sofre com conflito de sintaxe `${...}` do Quarkus.

**Se Python/pipx for uma opção:**

1. **Copier** — Melhor fit geral. Jinja2 sem conflito, seleção de domínios nativa, e `copier update` para propagar melhorias do template a projetos já gerados.
2. **Cookiecutter** — Mais simples que o Copier, mas sem `copier update` nem prompts condicionais nativos.

**Não recomendados**: Yeoman (em declínio), Quarkus CLI (não adequado), JHipster (overkill), Custom CLI (esforço desproporcional).

### Abordagem híbrida possível

- **Hygen** (ou Copier) como gerador principal parametrizado
- **GitHub Template Repository** como opção rápida para quem quer apenas clonar e customizar manualmente
