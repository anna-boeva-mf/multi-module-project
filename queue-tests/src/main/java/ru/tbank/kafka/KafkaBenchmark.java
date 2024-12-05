package ru.tbank.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public abstract class KafkaBenchmark {
    private List<KafkaProducer<String, String>> producers;
    private List<KafkaConsumer<String, String>> consumers;

    private final int producersCount;
    private final int consumersCount;

    private static final String TOPIC = "topic";
    private static final String KEY = "key";
    private static final String VALUE = "Message!";

    public KafkaBenchmark(int producersCount, int consumerNumber) {
        this.producersCount = producersCount;
        this.consumersCount = consumerNumber;
    }

    @Setup(Level.Trial)
    public void setup() {
        producers = new ArrayList<>();
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        for (int i = 0; i < producersCount; i++) {
            producers.add(new KafkaProducer<>(producerProps));
        }

        consumers = new ArrayList<>();
        Properties consumersProps = new Properties();
        consumersProps.put("bootstrap.servers", "localhost:29092");
        consumersProps.put("group.id", "group-id");
        consumersProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumersProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        for (int i = 0; i < consumersCount; i++) {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumersProps);
            consumer.subscribe(List.of(TOPIC));
            consumers.add(consumer);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        producers.forEach(KafkaProducer::close);
        consumers.forEach(KafkaConsumer::close);
    }

    @Benchmark
    public void kafkaTest(Blackhole blackhole) {
        producers.forEach(producer -> {
            blackhole.consume(producer.send(new ProducerRecord<>(TOPIC, KEY, VALUE)));
        });
        consumers.forEach(consumer -> {
            blackhole.consume(consumer.poll(Duration.ofMillis(100)));
        });
    }
}
