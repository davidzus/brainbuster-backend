# --- Project settings ---
PROJECT_NAME := brainbuster-backend
BASE_COMPOSE := docker-compose.yml
DEV_COMPOSE  := docker-compose.dev.yml
ENV_FILE     := .env               # compose auto-loads .env; keep for clarity

# safer branch detection
ACTIVE_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)

# default target
.PHONY: help
help:
	@echo "Make targets:"
	@echo "  dev-up        - Start dev stack (build if needed)"
	@echo "  dev-build     - Rebuild images, then start dev stack"
	@echo "  dev-down      - Stop & remove dev stack"
	@echo "  dev-restart   - Restart dev services"
	@echo "  dev-logs      - Tail logs for all dev services"
	@echo "  dev-ps        - Show dev services status"
	@echo "  app-sh        - Shell into app container (dev)"
	@echo "  mysql-sh      - MySQL client inside DB container (dev)"

# --- Dev targets ---
.PHONY: dev-up
dev-up:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		up -d

.PHONY: dev-build
dev-build:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		up -d --build

.PHONY: dev-down
dev-down:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		down

.PHONY: dev-restart
dev-restart:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		restart

.PHONY: dev-logs
dev-logs:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		logs -f

.PHONY: dev-ps
dev-ps:
	docker compose -p $(PROJECT_NAME) \
		-f $(BASE_COMPOSE) -f $(DEV_COMPOSE) \
		ps

# --- Handy shells ---
.PHONY: app-sh
app-sh:
	docker exec -it brainbuster-app /bin/sh

.PHONY: mysql-sh
mysql-sh:
	docker exec -it brainbuster-mysql mysql -u$${MYSQL_USER} -p$${MYSQL_PASSWORD} -e "SELECT 1;" $${MYSQL_DATABASE}
