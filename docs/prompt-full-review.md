# Complete project review

I want you to act as a senior software architect, expert in Java, Quarkus, modern backend, developer experience (DX), observability, and engineering best practices.

Your task is to perform a comprehensive analysis of a Java Quarkus project, focusing on simplicity, architectural clarity, code quality, developer productivity, future maintenance, and adherence to best practices.

Goal of the analyzed project:

- Educational project, simple but professional
- Based on Java + Quarkus
- REST API
- Structure ready to evolve
- Good experience for those who will develop, test, run locally, and maintain it
- Avoid unnecessary complexity
- Prioritize modern, pragmatic, and easy-to-understand solutions

I want your analysis to be deep, practical, and opinionated, but always balancing:

- simplicity
- best practices
- low coupling
- productivity
- ease of onboarding
- ease of debugging
- ease of deployment
- observability
- testability

Analyze the following aspects of the project:

1. Architecture overview

- Identify the architectural style of the project
- Say whether the architecture is suitable for a simple/educational project
- Point out complexity excesses or dangerous simplifications
- Suggest an ideal structure for small and medium Quarkus projects
- Evaluate separation of concerns, cohesion, and coupling

2. Package and layer organization

- Evaluate the directory and package structure
- Check whether the division between resource/controller, service/use case, repository, model/entity, dto, and mapper makes sense
- Suggest improvements to make the project more readable and maintainable
- Say whether it is worth organizing by layer or by feature
- Propose a recommended structure

3. Quarkus best practices

- Check whether the project uses Quarkus idiomatically
- Evaluate CDI usage, configuration, profiles, dev mode, extensions, and conventions
- Say if anything seems "over-Springified" and could be more natural in Quarkus
- Suggest useful and lean extensions for a modern REST project

4. REST API

- Evaluate endpoint design
- Route naming
- HTTP verbs
- Status codes
- Response standardization
- Error handling
- API versioning
- Pagination, filters, and sorting, if applicable
- Suggest practical improvements

5. DTOs, entities, and mapping

- Evaluate separation between persistence entities and objects exposed in the API
- Say whether the use of DTOs is appropriate or excessive
- Evaluate whether the mapping is clear
- Suggest whether or not to use MapStruct
- Point out risks of exposing entities directly
- Recommend a balanced approach for a simple project

6. Data persistence

- Evaluate the data access strategy
- Check the use of Panache, JPA/Hibernate, or another approach
- Say whether the choice makes sense for an educational project
- Evaluate clarity of repositories
- Suggest improvements for queries, transactions, and modeling
- Point out risks of excessive coupling to persistence

7. Validation and error handling

- Evaluate validations with Bean Validation
- Check consistency of error messages
- Evaluate global exception handling
- Suggest an error payload standard
- Say how to improve the experience for API consumers

8. Configuration and environment management

- Evaluate the use of application.properties
- Dev, test, and prod profiles
- Environment variables
- Local configuration
- Property clarity
- Suggest improvements to facilitate use by other developers
- Say how to avoid confusing configuration

9. Developer experience (DX)
   Pay special attention to this topic.
   Analyze:

- Ease of starting the project locally
- README clarity
- Minimum steps to run
- Use of Quarkus Dev Services
- Hot reload / live coding
- Local database setup
- Use of Docker Compose when it makes sense
- Useful scripts
- Makefile, task runner, or standardized commands
- Quality of error messages
- Ease of debugging
- Ease for new developers to understand the project

Suggest tools, practices, and automations that improve DX without over-complicating the project.

10. Code quality and consistency

- Evaluate readability
- Class, method, and variable names
- Method size and responsibility
- Possible code smells
- Duplication
- Unnecessary complexity
- Suggest practical improvements
- Point out where to apply Clean Code pragmatically, without excess

11. Tests

- Evaluate test strategy
- Unit tests
- Integration tests
- Resource/API tests
- Tests with database
- Use of REST Assured, JUnit, Mockito, or alternatives
- Ideal coverage for a simple project
- What to test first
- Suggest a realistic test pyramid for Quarkus

12. Observability and operations

- Evaluate logging
- Request correlation
- Health checks
- Metrics
- OpenAPI/Swagger
- OpenTelemetry/tracing
- Suggest the minimum ideal set for a small but professional project
- Differentiate what is essential from what is "nice to have"

13. Security

- Evaluate basic security aspects
- Input validation
- Sanitization
- Secrets handling
- Secure configuration
- CORS
- Authentication/authorization, if any
- Stack trace exposure
- Suggest a security baseline for a simple project

14. Build, dependencies, and maintenance

- Evaluate pom.xml or build.gradle organization
- Unnecessary dependencies
- Useful missing dependencies
- Recommended plugins
- Suggest improvements to keep the project lean
- Comment on dependency updates and technical governance

15. Code scalability and future evolution

- Say whether the project is prepared to grow without becoming a mess
- Which current decisions help or hinder evolution
- What to keep simple now
- What to prepare from the start
- What would be overengineering in this context

16. Practical recommendations
    At the end, deliver:

- Project strengths
- Project weaknesses
- Technical risks
- Short-term priority improvements
- Medium-term improvements
- Items unnecessary for this context
- Target architecture suggestion
- Minimum recommended stack suggestion
- Package structure suggestion
- Quality checklist suggestion

Expected response format:

1. Executive summary
2. Category evaluation, with a score from 0 to 10
3. Issues found
4. Objective improvement suggestions
5. Recommended structure example
6. Recommended stack example for a simple and modern Quarkus project
7. Evolution roadmap by phases
8. Final conclusion with architectural recommendation

Important:

- Be specific and practical
- Avoid generic responses
- Explain trade-offs
- Clearly differentiate what is essential from what is optional
- Do not propose unnecessary complexity
- Consider that the project is simple and educational, but should follow a professional standard
- Whenever possible, suggest simpler alternatives before more sophisticated ones
- Prioritize developer productivity and project clarity
