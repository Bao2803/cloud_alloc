all:
	./gradlew build && docker-compose build && docker-compose up -d

clean:
	docker compose down
	docker volume prune -f