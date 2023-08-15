package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모(){
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); //다른 스레드에서 수행하는 작업을 기다려줌
        for(int i = 0; i < threadCount ;i++){
            long userId = i;
            executorService.submit(() -> {
                try{
                    applyService.apply(userId); //Kafka 적용후 테스트케이스가 실패한 이유는 실시간이 아니기 때문
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Thread.sleep(10000);

        long count = couponRepository.count();
        assertThat(count).isEqualTo(100L);
        /*
            expected: 100L
            but was: 301L
            org.opentest4j.AssertionFailedError:
            expected: 100L
            but was: 301L
         */

        //발생 이유 : Race Condition 발생 -> 두 개 이상의 스레드가 공유 데이터에 엑세스를 하고 동시에 작업을 하려고 할 때 발생
    }




    @Test
    public void 한명당_한개의_쿠폰만_발급() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); //다른 스레드에서 수행하는 작업을 기다려줌
        for(int i = 0; i < threadCount ;i++){
            long userId = i;
            executorService.submit(() -> {
                try{
                    applyService.apply(1L); //Kafka 적용후 테스트케이스가 실패한 이유는 실시간이 아니기 때문
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Thread.sleep(10000);

        long count = couponRepository.count();
        assertThat(count).isEqualTo(1L);
    }

}