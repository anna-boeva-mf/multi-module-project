package ru.tbank.rabbit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public abstract class RabbitBenchmark {
    private List<RabbitProducer> producers;
    private List<RabbitConsumer> consumers;

    private final int producersCount;
    private final int consumersCount;

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static final String MESSAGE = "Message!";

    public RabbitBenchmark(int producersCount, int consumerCount) {
        this.producersCount = producersCount;
        this.consumersCount = consumerCount;
    }

    @Setup(Level.Trial)
    public void setup() throws IOException, TimeoutException {
        producers = new ArrayList<>();
        for (int i = 0; i < producersCount; i++) {
            producers.add(new RabbitProducer(HOST, PORT, USERNAME, PASSWORD));
        }

        consumers = new ArrayList<>();
        for (int i = 0; i < consumersCount; i++) {
            consumers.add(new RabbitConsumer(HOST, PORT, USERNAME, PASSWORD));
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        consumers.forEach(RabbitConsumer::close);
        producers.forEach(RabbitProducer::close);
    }

    @Benchmark
    public void rabbitTest(Blackhole blackhole) {

        producers.forEach(producer -> {
            try {
                producer.send(MESSAGE);
                blackhole.consume(MESSAGE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        consumers.forEach(consumer -> {
            try {
                blackhole.consume(consumer.consume());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
