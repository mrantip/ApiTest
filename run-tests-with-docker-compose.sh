#!/bin/bash

# Переменные окружения для тестов
export API_BASE_URL="${API_BASE_URL:-http://localhost:4111}"
export UI_BASE_URL="${UI_BASE_URL:-http://localhost:3000}"
export SELENOID_URL="${SELENOID_URL:-http://localhost:4444}"
export SELENOID_UI_URL="${SELENOID_UI_URL:-http://localhost:8080}"
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5433/nbank}"
export DB_USERNAME="${DB_USERNAME:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres}"

# Настройки Docker
COMPOSE_DIR="${COMPOSE_DIR:-./infra/docker_compose}"
DOCKER_COMPOSE_FILE="${DOCKER_COMPOSE_FILE:-$COMPOSE_DIR/docker-compose.yml}"
TEST_CONTAINER_NAME="nbank-tests-runner"

docker_compose_cmd() {
    if docker compose version &> /dev/null; then
        docker compose -f "$DOCKER_COMPOSE_FILE" "$@"
    else
        docker-compose -f "$DOCKER_COMPOSE_FILE" "$@"
    fi
}

cleanup() {
    local exit_code=$?

    echo ""
    echo "ОСТАНОВКА ОКРУЖЕНИЯ"

    # Проверяем, что контейнер с тестами удален (--rm уже должен был сделать это)
    if docker ps -a --format '{{.Names}}' | grep -q "^$TEST_CONTAINER_NAME$"; then
        echo "⚠️ Контейнер с тестами не был удален автоматически. Удаляем вручную..."
        docker rm -f "$TEST_CONTAINER_NAME" 2>/dev/null || true
    else
        echo "✅ Контейнер с тестами удален автоматически (--rm)"
    fi

    # Останавливаем Docker Compose
    echo "⏹️ Остановка Docker Compose..."
    docker_compose_cmd down -v --remove-orphans 2>/dev/null || true

    echo "✅ Окружение остановлено"
    echo ""

    if [ $exit_code -eq 0 ]; then
        echo "✅ Все тесты успешно пройдены! 🎉"
    else
        echo "❌ Тесты завершились с ошибкой (код: $exit_code)"
        echo "💡 Проверьте логи выше для получения подробной информации."
    fi

    exit $exit_code
}

trap cleanup EXIT INT TERM

main() {

    echo "ПОДНЯТИЕ ТЕСТОВОГО ОКРУЖЕНИЯ"

    echo "Запуск Docker Compose..."
    docker_compose_cmd up -d

    if [ $? -ne 0 ]; then
        echo "Ошибка запуска Docker Compose!"
        exit 1
    fi
    echo "Docker Compose окружение запущено"

    echo "Состояние контейнеров:"
    docker_compose_cmd ps


    echo "Запуск тестов в контейнере '$DOCKER_IMAGE_NAME'..."
    echo "Переменные окружения:"
    echo "  API_BASE_URL: $API_BASE_URL"
    echo "  UI_BASE_URL: $UI_BASE_URL"
    echo "  SELENOID_URL: $SELENOID_URL"
    echo "  SELENOID_UI_URL: $SELENOID_UI_URL"
    echo "  DB_URL: $DB_URL"

    echo ""

    # Запускаем контейнер с тестами
    docker run --rm \
        --name "$TEST_CONTAINER_NAME" \
        --network nbank-network \
        -e API_BASE_URL="$API_BASE_URL" \
        -e UI_BASE_URL="$UI_BASE_URL" \
        -e SELENOID_URL="$SELENOID_URL" \
        -e SELENOID_UI_URL="$SELENOID_UI_URL" \
        -e DB_URL="$DB_URL" \
        -e DB_USERNAME="$DB_USERNAME" \
        -e DB_PASSWORD="$DB_PASSWORD" \
        "$DOCKER_IMAGE_NAME"

    TEST_EXIT_CODE=$?

    echo ""

    if [ $TEST_EXIT_CODE -eq 0 ]; then
        echo "✅ Все тесты успешно пройдены!"
    else
        echo "❌ Тесты завершились с ошибкой (код: $TEST_EXIT_CODE)"
    fi

    #ОСТАНОВКА ОКРУЖЕНИЯ
    echo "ЗАВЕРШЕНИЕ"

    if [ $TEST_EXIT_CODE -eq 0 ]; then
        echo "🎉 Тесты успешно завершены!"
    else
        echo "💥 Тесты завершились с ошибкой. Проверьте логи выше."
    fi

    exit $TEST_EXIT_CODE
}

# Проверяем, что скрипт запущен напрямую, а не импортирован
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi