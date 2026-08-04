#!/bin/bash

# Настройка
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-mrantip}"
IMAGE_NAME="${IMAGE_NAME:-nbank-tests}"
TAG="${TAG:-latest}"
LOCAL_IMAGE_NAME="nbank-tests:latest"

docker build -t "$LOCAL_IMAGE_NAME" .

# Авторизация
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

# Тегирование
FULL_IMAGE_NAME="$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

docker tag "$LOCAL_IMAGE_NAME" "$FULL_IMAGE_NAME"

# Пуш
docker push "$FULL_IMAGE_NAME"

# Финальное сообщение
echo "📦 Скачать образ можно командой:"
echo -e "docker pull $FULL_IMAGE_NAME"
echo ""
echo "Запустить тесты в контейнере:"
echo "docker run --rm \\"
echo "  -e API_BASE_URL=http://your-api-host:4111 \\"
echo "  -e DB_URL=jdbc:postgresql://your-db-host:5433/nbank \\"
echo "  -e DB_USERNAME=postgres \\"
echo "  -e DB_PASSWORD=postgres \\"
echo "  $FULL_IMAGE_NAME"
echo ""
echo "📋 Информация об образе:"
echo "  Username:  $DOCKERHUB_USERNAME"
echo "  Image:     $IMAGE_NAME"
echo "  Tag:       $TAG"
echo "  Full:      $FULL_IMAGE_NAME"
echo ""
echo "Ссылка на Docker Hub:"
echo "  https://hub.docker.com/r/$DOCKERHUB_USERNAME/$IMAGE_NAME/tags"