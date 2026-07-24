#!/bin/sh
set -eu

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD obrigatorio}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE obrigatorio}"
: "${MYSQL_USER:?MYSQL_USER obrigatorio}"
: "${MYSQL_PASSWORD:?MYSQL_PASSWORD obrigatorio}"

MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_NETWORK_TEST_HOST="${MYSQL_NETWORK_TEST_HOST:-db}"
READY_FILE="${CPA_DB_USER_READY_FILE:-/tmp/cpa-app-user-ready}"

escape_sql_string() {
  printf "%s" "$1" | sed "s/'/''/g"
}

escape_identifier() {
  printf "%s" "$1" | sed 's/`/``/g'
}

db_name="$(escape_identifier "$MYSQL_DATABASE")"
app_user="$(escape_sql_string "$MYSQL_USER")"
app_password="$(escape_sql_string "$MYSQL_PASSWORD")"

app_user_can_connect() {
  mysql -h "$MYSQL_NETWORK_TEST_HOST" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
    --connect-timeout=3 -e "SELECT 1" >/dev/null 2>&1
}

if app_user_can_connect; then
  touch "$READY_FILE"
  exit 0
fi

if [ -f "$READY_FILE" ]; then
  exit 0
fi

if ! mysql -h "$MYSQL_HOST" -uroot -p"$MYSQL_ROOT_PASSWORD" --connect-timeout=3 -e "SELECT 1" >/dev/null 2>&1; then
  echo "MySQL iniciou, mas MYSQL_ROOT_PASSWORD nao autentica no volume atual e ${MYSQL_USER} ainda nao acessa pela rede Docker." >&2
  echo "Use a senha root original desse volume ou recrie o volume em ambiente descartavel de desenvolvimento." >&2
  exit 1
fi

mysql -h "$MYSQL_HOST" -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS \`${db_name}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${app_user}'@'%' IDENTIFIED BY '${app_password}';
ALTER USER '${app_user}'@'%' IDENTIFIED BY '${app_password}';
GRANT ALL PRIVILEGES ON \`${db_name}\`.* TO '${app_user}'@'%';
FLUSH PRIVILEGES;
SQL

if ! app_user_can_connect; then
  echo "Usuario ${MYSQL_USER} foi configurado, mas ainda nao conseguiu autenticar pela rede Docker." >&2
  exit 1
fi

touch "$READY_FILE"
echo "Usuario da aplicacao validado para acesso pela rede Docker."
