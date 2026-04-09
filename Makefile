.DEFAULT_GOAL := help

.PHONY: help dev check fmt build docker

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

dev: ## Start application in development mode (hot reload)
	./run.sh

check: ## Run tests and validate code formatting
	./run-check.sh

fmt: ## Auto-fix Java code formatting with Spotless
	./run-spotless-apply.sh

build: ## Build production artefacts with clean verify
	./run-build-prod.sh

docker: ## Build JVM Docker image and run the container
	./run-docker.sh
