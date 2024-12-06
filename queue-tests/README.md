### Тестирование производительности RabbitMQ и Kafka с использованием JMH
Модуль queue-tests проекта. Для запуска тестов поднять сервисы в контекйнерах и запустить JMH:
```bash
cd ./queue-tests
docker-compose up -d
gradle jmh
```
Результаты тестов записываются в файл ./build/results/jmh/results.txt



