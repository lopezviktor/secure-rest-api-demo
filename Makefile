COMPOSE = docker compose -f docker-compose.yml -f docker-compose.grafana.yml

.PHONY: up down restart ps logs logs-db logs-prom logs-grafana reset-db

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down

restart:
	$(COMPOSE) down
	$(COMPOSE) up -d

ps:
	$(COMPOSE) ps

logs:
	$(COMPOSE) logs -f --tail=200

logs-db:
	$(COMPOSE) logs -f --tail=200 db

logs-prom:
	$(COMPOSE) logs -f --tail=200 prometheus

logs-grafana:
	$(COMPOSE) logs -f --tail=200 grafana

reset-db:
	$(COMPOSE) down -v
	$(COMPOSE) up -d