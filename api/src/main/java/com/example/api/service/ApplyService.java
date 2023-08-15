package com.example.api.service;

import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplyService {
    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;

    private final AppliedUserRepository appliedUserRepository;

    public ApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository, CouponCreateProducer couponCreateProducer, AppliedUserRepository appliedUserRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.appliedUserRepository = appliedUserRepository;
    }


    //쿠폰 발급 로직
    public void apply(Long userId) {
        //10:01 1번 사용자
        //10:02 발급완료
        // 2, 3, 4번 사용자
        //synchronized는 서버가 여러대인 경우 의미없음
        //redis incr key : value -> 키를 1씩 증가
        //redis single thread로 정합성 관리 가능
        //incr 속도도 빠름
//        long count = couponRepository.count();

        //lock start
        //쿠폰발급 여부
        //if(발급됐다면) return;
        Long apply = appliedUserRepository.add(userId);
        if(apply != 1){
            return;
        }
        Long count = couponCountRepository.increment();
        if (count > 100) {
            return;
        }
//        couponRepository.save(new Coupon(userId)); //2초
        couponCreateProducer.create(userId);

        //lock end
    }
}
