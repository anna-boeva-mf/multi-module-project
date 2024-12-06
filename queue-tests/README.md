### Тестирование производительности RabbitMQ и Kafka с использованием JMH
Модуль queue-tests проекта. Для запуска тестов поднять сервисы и запустить JMH:
```bash
cd ./queue-tests
docker-compose up -d
gradle jmh
```
Количество прогонов бенчмарка, итераций и прогревочных итераций можно менять.

Результаты тестов будут записаны в файл ./build/results/jmh/results.txt

#### Выводы:
RabbitMQ показывает значительно более высокую пропускную способность во всех конфигурациях по сравнению с Kafka, RabbitMQ обрабатывает сообщения в десятки раз быстрее.

Время отклика RabbitMQ также значительно меньше, чем у Kafka. Это делает RabbitMQ более подходящим, когда важна быстрая доставка сообщений и минимальная задержка.

Kafka, в свою очередь, лучше подходит при необходимости высокой надежности и возможности обработки большого объема данных, особенно с высокой нагрузкой, где его архитектура позволяет эффективно обрабатывать большие потоки данных, хотя и с более высокой задержкой.

Выбор между RabbitMQ и Kafka зависит от конкретных требований создаваемого приложения. Когда приоритетом является высокая пропускная способность и низкая задержка, RabbitMQ будет лучшим выбором. Если же важна надежность и обработка больших потоков данных, то стоит присмотреться к Kafka, несмотря на более высокую задержку.

-----07.12.2024

Большое количество ошибок у RabbitMQ выглядит как проблема с забором сообщений. Причем судя по логам с каждой новой итерацией в рамках одного теста пропускная способность регрессирует, как будто сообщения просто со временем перестают успевать обрабатываться.
```bash
# Fork: 1 of 1
# Warmup Iteration   1: 1054,591 ops/s
# Warmup Iteration   2: 954,380 ops/s
Iteration   1: 804,601 ops/sG [7s]
Iteration   2: 763,458 ops/sG [8s]
Iteration   3: 626,252 ops/sG [9s]
Iteration   4: 690,586 ops/sG [10s]
Iteration   5: 547,149 ops/sG [11s]
Iteration   6: 337,593 ops/sG [12s]
Iteration   7: 406,474 ops/sG [13s]
Iteration   8: 464,050 ops/sG [14s]
Iteration   9: 460,584 ops/sG [15s]
Iteration  10: 271,719 ops/sG [16s]


Result "ru.tbank.rabbit.RabbitSimpleTest.rabbitTest":
537,247 ▒(99.9%) 272,734 ops/s [Average]
(min, avg, max) = (271,719, 537,247, 804,601), stdev = 180,397
CI (99.9%): [264,513, 809,981] (assumes normal distribution)
```

Попробовала в тесте добавить паузу, чтобы сообщения успели обработаться. Пропускная способность из-за пауз снизилась, но стала стабильна. А процент ошибок резко снизился.
```bash
# Fork: 1 of 1
# Warmup Iteration   1: 54,513 ops/s
# Warmup Iteration   2: 60,689 ops/s
Iteration   1: 59,921 ops/sNG [9s]
Iteration   2: 58,887 ops/sNG [10s]
Iteration   3: 60,010 ops/sNG [11s]
Iteration   4: 59,364 ops/sNG [12s]
Iteration   5: 60,926 ops/sNG [13s]
Iteration   6: 57,251 ops/sNG [14s]
Iteration   7: 60,146 ops/sNG [15s]
Iteration   8: 59,913 ops/sNG [16s]
Iteration   9: 59,163 ops/sNG [17s]
Iteration  10: 61,043 ops/sNG [18s]


Result "ru.tbank.rabbit.RabbitSimpleTest.rabbitTest":
59,662 ▒(99.9%) 1,652 ops/s [Average]
(min, avg, max) = (57,251, 59,662, 61,043), stdev = 1,093
CI (99.9%): [58,011, 61,314] (assumes normal distribution)

```
Если у RabbitMQ добавить подтверждение сообщений, то пропускная способность тоже снижается, но и количество ошибок уменьшается.
Пример:
```bash
RabbitSimpleTest.rabbitTest                             thrpt   10  729,493 ± 254,377  ops/s
RabbitSimpleTest_confirm.rabbitTest                     thrpt   10  403,679 ±  44,821  ops/s
RabbitSimpleTest_sleep.rabbitTest                       thrpt   10   62,170 ±   1,685  ops/s
```