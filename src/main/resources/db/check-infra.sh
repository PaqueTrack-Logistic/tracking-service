#!/usr/bin/env bash
set -euo pipefail

POSTGRES_CONTAINER="tracking-postgres"
RABBIT_CONTAINER="tracking-rabbitmq"
PG_USER="tracking_user"
PG_DB="tracking_db"
PG_PASSWORD="secret"

failures=0

check_running_service() {
  local service="$1"
  if docker compose ps --status running --services | grep -qx "$service"; then
    echo "[OK] Servicio '$service' esta en ejecucion"
  else
    echo "[ERROR] Servicio '$service' no esta en ejecucion"
    failures=$((failures + 1))
  fi
}

echo "== Verificando servicios Docker =="
check_running_service "postgres"
check_running_service "rabbitmq"

echo
echo "== Verificando RabbitMQ =="
if docker exec "$RABBIT_CONTAINER" rabbitmq-diagnostics -q ping >/dev/null; then
  echo "[OK] RabbitMQ responde ping"
else
  echo "[ERROR] RabbitMQ no responde ping"
  failures=$((failures + 1))
fi

echo
echo "== Verificando tablas en PostgreSQL =="
if tables=$(docker exec -e PGPASSWORD="$PG_PASSWORD" "$POSTGRES_CONTAINER" psql -U "$PG_USER" -d "$PG_DB" -At -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"); then
  echo "Tablas detectadas:"
  if [[ -n "$tables" ]]; then
    echo "$tables"
  else
    echo "(ninguna)"
  fi

  if echo "$tables" | grep -qx "shipment" && echo "$tables" | grep -qx "tracking_event"; then
    echo "[OK] Tablas esperadas encontradas (shipment, tracking_event)"
  else
    echo "[ERROR] Faltan tablas esperadas (shipment, tracking_event)"
    failures=$((failures + 1))
  fi
else
  echo "[ERROR] No fue posible consultar PostgreSQL"
  failures=$((failures + 1))
fi

echo
if [[ "$failures" -eq 0 ]]; then
  echo "Resultado final: TODO OK"
  exit 0
fi

echo "Resultado final: $failures validacion(es) fallaron"
exit 1
