package com.xiaoliu.seckill.controller;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueTest {

    //生产者
    public static class Producer implements Runnable{

        private final BlockingQueue<Integer> blockingQueue;
        private volatile boolean flag;
        private Random random;

        public Producer(BlockingQueue<Integer> blockingQueue) {
            this.blockingQueue = blockingQueue;
            flag = false;
            random = new Random();
        }


        @Override
        public void run() {
            while (!flag){
                int i = random.nextInt(100);

                try {
                    blockingQueue.put(i);
                    System.out.println(Thread.currentThread().getName()+" produce "+ i);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public void shutDown(){
            flag = true;
        }
    }


    //消费者
    public static class Consumer implements Runnable{
        private final BlockingQueue<Integer> blockingQueue;
        private volatile boolean flag;
        private Random random;

        public Consumer(BlockingQueue<Integer> blockingQueue) {
            this.blockingQueue = blockingQueue;
        }


        @Override
        public void run() {
            while ( !flag){
                int info;

                try {
                    info = blockingQueue.take();
                    System.out.println(Thread.currentThread().getName() + "consumer" + info);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public void shutDown(){
            flag = true;
        }
    }


    public static void main(String[] args) {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<Integer>(5);
        Producer producer = new Producer(blockingQueue);
        Consumer consumer = new Consumer(blockingQueue);
        for (int i = 0; i < 10; i++) {
            if (i < 5 ){
                new Thread(producer,"producer"+i).start();
            }else {
                new Thread(consumer,"consumer"+ (i-5)).start();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        producer.shutDown();
        consumer.shutDown();

    }


}
