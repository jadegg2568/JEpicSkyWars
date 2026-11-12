package io.github.jadegg2568.task;

public interface IGameTask {
    void start();

    void cancel();

    boolean isRunning();

    int getTime();
}
