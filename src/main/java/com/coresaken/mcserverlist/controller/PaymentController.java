package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.payment.PaymentDto;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    final PaymentService paymentService;

    @GetMapping("/server/payment/{id}/{promotionPoints}")
    public ResponseEntity<ObjectResponse<PaymentDto>> getPromotionPointPaymentDto(@PathVariable("id") Long serverId,
                                                         @PathVariable("promotionPoints") int promotionPointsAmount) {
        return paymentService.getPromotionPointPaymentDto(serverId, promotionPointsAmount);
    }

    @GetMapping("/banner/payment/{id}")
    public ResponseEntity<ObjectResponse<PaymentDto>> getBannerPaymentDto(@PathVariable("id") Long id) {
        return paymentService.getBannerPaymentDto(id);
    }

    @PostMapping("/payment-notification")
    public String getPaymentNotification(
            @RequestParam(required = false) String ID_ZAMOWIENIA,
            @RequestParam(required = false) String KWOTA,
            @RequestParam(required = false) String ID_PLATNOSCI,
            @RequestParam(required = false) String STATUS,
            @RequestParam(required = false) String SECURE,
            @RequestParam(required = false) String SEKRET,
            @RequestParam(required = false) String HASH) {

        return paymentService.handlePayments(ID_ZAMOWIENIA, KWOTA, ID_PLATNOSCI, STATUS, SECURE, SEKRET, HASH);
    }
}
