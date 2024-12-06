package ru.tbank.rabbit;

public class RabbitStressTest_confirm extends RabbitBenchmark {
    public RabbitStressTest_confirm() {
        super(10, 10, false,true);
    }
}