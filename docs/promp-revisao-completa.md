Quero que você atue como um arquiteto de software sênior, especialista em Java, Quarkus, backend moderno, experiência do desenvolvedor (DX), observabilidade e boas práticas de engenharia.

Sua tarefa é fazer uma análise completa de um projeto Java Quarkus, com foco em simplicidade, clareza arquitetural, qualidade de código, produtividade do desenvolvedor, manutenção futura e aderência a boas práticas.

Objetivo do projeto analisado:

- Projeto didático, simples, mas profissional
- Baseado em Java + Quarkus
- API REST
- Estrutura pronta para evoluir
- Boa experiência para quem vai desenvolver, testar, rodar localmente e dar manutenção
- Evitar complexidade desnecessária
- Priorizar soluções modernas, pragmáticas e fáceis de entender

Quero que sua análise seja profunda, prática e opinativa, mas sempre equilibrando:

- simplicidade
- boas práticas
- baixo acoplamento
- produtividade
- facilidade de onboarding
- facilidade de debug
- facilidade de deploy
- observabilidade
- testabilidade

Analise os seguintes aspectos do projeto:

1. Visão geral da arquitetura

- Identifique o estilo arquitetural do projeto
- Diga se a arquitetura está adequada para um projeto simples/didático
- Aponte excessos de complexidade ou simplificações perigosas
- Sugira uma estrutura ideal para projetos pequenos e médios em Quarkus
- Avalie separação de responsabilidades, coesão e acoplamento

2. Organização de pacotes e camadas

- Avalie a estrutura de diretórios e pacotes
- Verifique se a divisão entre resource/controller, service/use case, repository, model/entity, dto e mapper faz sentido
- Sugira melhorias para deixar o projeto mais legível e fácil de manter
- Diga se vale a pena organizar por camada ou por funcionalidade
- Proponha uma estrutura recomendada

3. Boas práticas com Quarkus

- Verifique se o projeto usa Quarkus de forma idiomática
- Avalie uso de CDI, configuração, profiles, dev mode, extensions e conventions
- Diga se há algo que parece “Springficado” demais e poderia ser mais natural em Quarkus
- Sugira extensões úteis e enxutas para um projeto REST moderno

4. API REST

- Avalie design dos endpoints
- Nomenclatura de rotas
- Verbos HTTP
- Códigos de status
- Padronização de respostas
- Tratamento de erros
- Versionamento de API
- Paginação, filtros e ordenação, se aplicável
- Sugira melhorias práticas

5. DTOs, entidades e mapeamento

- Avalie separação entre entidades de persistência e objetos expostos na API
- Diga se o uso de DTOs está bom ou exagerado
- Avalie se o mapeamento está claro
- Sugira uso ou não de MapStruct
- Aponte riscos de expor entidade diretamente
- Recomende uma abordagem equilibrada para projeto simples

6. Persistência de dados

- Avalie a estratégia de acesso a dados
- Verifique uso de Panache, JPA/Hibernate ou outra abordagem
- Diga se a escolha faz sentido para um projeto didático
- Avalie clareza dos repositories
- Sugira melhorias para queries, transações e modelagem
- Aponte riscos de acoplamento excessivo à persistência

7. Validação e tratamento de erros

- Avalie validações com Bean Validation
- Verifique consistência das mensagens de erro
- Avalie tratamento global de exceções
- Sugira um padrão de payload de erro
- Diga como melhorar a experiência de quem consome a API

8. Configuração e gerenciamento de ambientes

- Avalie uso de application.properties
- Profiles dev, test e prod
- Variáveis de ambiente
- Configuração local
- Clareza das propriedades
- Sugira melhorias para facilitar uso por outros desenvolvedores
- Diga como evitar configuração confusa

9. Experiência do desenvolvedor (DX)
   Quero atenção especial a este tópico.
   Analise:

- Facilidade para subir o projeto localmente
- Clareza do README
- Passos mínimos para rodar
- Uso de Quarkus Dev Services
- Hot reload / live coding
- Setup de banco local
- Uso de Docker Compose quando fizer sentido
- Scripts úteis
- Makefile, task runner ou comandos padronizados
- Qualidade das mensagens de erro
- Facilidade de debug
- Facilidade para novos desenvolvedores entenderem o projeto

Sugira ferramentas, práticas e automações que melhorem a DX sem complicar demais o projeto.

10. Qualidade de código e consistência

- Avalie legibilidade
- Nomes de classes, métodos e variáveis
- Tamanho e responsabilidade dos métodos
- Possíveis code smells
- Duplicação
- Complexidade desnecessária
- Sugira melhorias práticas
- Aponte onde aplicar Clean Code com pragmatismo, sem exageros

11. Testes

- Avalie estratégia de testes
- Testes unitários
- Testes de integração
- Testes de resource/API
- Testes com banco
- Uso de REST Assured, JUnit, Mockito ou alternativas
- Cobertura ideal para um projeto simples
- O que testar primeiro
- Sugira uma pirâmide de testes realista para Quarkus

12. Observabilidade e operação

- Avalie logging
- Correlação de requisições
- Health checks
- Métricas
- OpenAPI/Swagger
- OpenTelemetry/tracing
- Sugira o conjunto mínimo ideal para um projeto pequeno, mas profissional
- Diferencie o que é essencial do que é “nice to have”

13. Segurança

- Avalie aspectos básicos de segurança
- Validação de entrada
- Sanitização
- Tratamento de segredos
- Configuração segura
- CORS
- Autenticação/autorização, se houver
- Exposição de stacktrace
- Sugira baseline de segurança para projeto simples

14. Build, dependências e manutenção

- Avalie organização do pom.xml ou build.gradle
- Dependências desnecessárias
- Dependências úteis faltando
- Plugins recomendados
- Sugira melhorias para manter o projeto enxuto
- Comente sobre atualização de dependências e governança técnica

15. Escalabilidade de código e evolução futura

- Diga se o projeto está preparado para crescer sem virar bagunça
- Quais decisões atuais ajudam ou atrapalham evolução
- O que manter simples agora
- O que preparar desde já
- O que seria overengineering neste contexto

16. Recomendações práticas
    Ao final, entregue:

- Pontos fortes do projeto
- Pontos fracos
- Riscos técnicos
- Melhorias prioritárias de curto prazo
- Melhorias de médio prazo
- Itens desnecessários para este contexto
- Sugestão de arquitetura alvo
- Sugestão de stack mínima recomendada
- Sugestão de estrutura de pacotes
- Sugestão de checklist de qualidade

Formato de resposta esperado:

1. Resumo executivo
2. Avaliação por categoria, com nota de 0 a 10
3. Problemas encontrados
4. Sugestões objetivas de melhoria
5. Exemplo de estrutura recomendada
6. Exemplo de stack recomendada para projeto Quarkus simples e moderno
7. Roadmap de evolução em fases
8. Conclusão final com recomendação arquitetural

Importante:

- Seja específico e prático
- Evite respostas genéricas
- Explique trade-offs
- Diferencie claramente o que é essencial do que é opcional
- Não proponha complexidade desnecessária
- Considere que o projeto é simples e didático, mas deve seguir padrão profissional
- Sempre que possível, sugira alternativas mais simples antes das mais sofisticadas
- Priorize produtividade do desenvolvedor e clareza do projeto
