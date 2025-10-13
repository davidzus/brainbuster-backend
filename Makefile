PROJECT_NAME := "BrainBuster"
DOCKER_COMPOSE_FILE := $(shell pwd)/compose.yaml
ACTIVE_BRANCH := $(shell git branch -v | grep '*' | tr -d '*' | tr -d ' ')

#Docker

start:
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d

stop:
	docker compose -f $(DOCKER_COMPOSE_FILE) down

restart:
	docker compose -f $(DOCKER_COMPOSE_FILE) restart

logs:
	docker compose -f $(DOCKER_COMPOSE_FILE) logs -f