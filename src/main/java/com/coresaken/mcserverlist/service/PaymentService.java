package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.payment.Banner;
import com.coresaken.mcserverlist.data.payment.PaymentAction;
import com.coresaken.mcserverlist.data.payment.PaymentDto;
import com.coresaken.mcserverlist.data.payment.PromotionPoints;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.database.model.Payment;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.PaymentRepository;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${hotpay.secret}")
    String secret;
    @Value("${hotpay.password}")
    String password;
    @Value("${website.address}")
    String websiteAddress;
    final ObjectMapper mapper = new ObjectMapper();

    final PaymentRepository paymentRepository;

    final ServerService serverService;
    final BannerService bannerService;
    final PromotionPointService promotionPointService;

    public ResponseEntity<ObjectResponse<PaymentDto>> getPromotionPointPaymentDto(Long serverId, int promotionPointsAmount) {
        Server server = serverService.getServerById(serverId);
        if(server == null){
            return ObjectResponse.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje!");
        }

        if(promotionPointsAmount < 7 || promotionPointsAmount > 365){
            return ObjectResponse.badRequest(2, "Liczba punktów promowania powinna być z przedziału <7,365>!");
        }

        PromotionPoints promotionPoints = new PromotionPoints();
        promotionPoints.setServerId(server.getId());
        promotionPoints.setPromotionPoints(promotionPointsAmount);
        String successAction;

        try{
            successAction = mapper.writeValueAsString(promotionPoints);
        }catch (JsonProcessingException e) {
            return ObjectResponse.badRequest(3, "Wystąpił nieoczekiwany błąd.");
        }

        Payment payment = new Payment();
        payment.setAction(PaymentAction.PROMOTION_POINTS);
        payment.setServiceId(generateRandomServiceId());
        payment.setStatus(Payment.Status.DEFAULT);
        payment.setSuccessAction(successAction);
        payment = paymentRepository.save(payment);
        int price = promotionPointsAmount * 2;

        PaymentDto paymentDto = getPromotionPaymentDto(server.getIp(), payment.getId(), String.valueOf(price));
        return ObjectResponse.ok("", paymentDto);
    }
    private PaymentDto getPromotionPaymentDto(String ip, Long orderId, String price){
        PaymentDto paymentDto = new PaymentDto();

        paymentDto.setSecret(secret);
        paymentDto.setAmount(price);
        paymentDto.setServiceName("Punkty promowania dla serwera: " + ip);
        paymentDto.setWebsiteAddress(websiteAddress);
        paymentDto.setOrderId(String.valueOf(orderId));

        String stringBuilder = password + ";" +
                paymentDto.getAmount() + ";" +
                paymentDto.getServiceName() + ";" +
                paymentDto.getWebsiteAddress() + ";" +
                paymentDto.getOrderId() + ";" +
                paymentDto.getSecret();
        String hash = hashSHA256(stringBuilder);
        paymentDto.setHash(hash);

        return paymentDto;
    }

    public ResponseEntity<ObjectResponse<PaymentDto>> getBannerPaymentDto(Long id) {
        com.coresaken.mcserverlist.database.model.Banner banner = bannerService.getById(id);
        if(banner == null){
            return ObjectResponse.badRequest(1, "Wystąpił nieoczekiwany błąd. Baner o podanym ID nie istnieje!");
        }

        Banner bannerPayment = new Banner();
        bannerPayment.setBannerId(id);

        String successAction;
        try{
            successAction = mapper.writeValueAsString(bannerPayment);
        }catch (JsonProcessingException e) {
            return ObjectResponse.badRequest(2, "Wystąpił nieoczekiwany błąd!");
        }

        Payment payment = new Payment();
        payment.setAction(PaymentAction.BANNER);
        payment.setServiceId(generateRandomServiceId());
        payment.setStatus(Payment.Status.DEFAULT);
        payment.setSuccessAction(successAction);
        payment = paymentRepository.save(payment);
        String price;
        if(banner.getSize()== com.coresaken.mcserverlist.database.model.Banner.Size.BIG){
            price = "100";
        }
        else {
            price = "30";
        }

        PaymentDto paymentDto = getBannerPaymentDto(payment.getId(), price);
        return ObjectResponse.ok("", paymentDto);
    }
    private PaymentDto getBannerPaymentDto(Long orderId, String price){
        PaymentDto paymentDto = new PaymentDto();

        paymentDto.setSecret(secret);
        paymentDto.setAmount(price);
        paymentDto.setServiceName("Banner reklamowy");
        paymentDto.setWebsiteAddress(websiteAddress);
        paymentDto.setOrderId(String.valueOf(orderId));

        String stringBuilder = password + ";" +
                paymentDto.getAmount() + ";" +
                paymentDto.getServiceName() + ";" +
                paymentDto.getWebsiteAddress() + ";" +
                paymentDto.getOrderId() + ";" +
                paymentDto.getSecret();
        String hash = hashSHA256(stringBuilder);
        paymentDto.setHash(hash);

        return paymentDto;
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String generateRandomServiceId() {
        return UUID.randomUUID().toString();
    }

    public String handlePayments(
            String ID_ZAMOWIENIA,
            String KWOTA,
            String ID_PLATNOSCI,
            String STATUS,
            String SECURE,
            String SEKRET,
            String HASH) {

        // Obsługa parametrów
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(password).append(";");
        if (KWOTA != null) {
            stringBuilder.append(KWOTA).append(";");
        }
        if (ID_PLATNOSCI != null) {
            stringBuilder.append(ID_PLATNOSCI).append(";");
        }
        if (ID_ZAMOWIENIA != null) {
            stringBuilder.append(ID_ZAMOWIENIA).append(";");
        }
        if (STATUS != null) {
            stringBuilder.append(STATUS).append(";");
        }
        if (SECURE != null) {
            stringBuilder.append(SECURE).append(";");
        }
        if (SEKRET != null) {
            stringBuilder.append(SEKRET);
        }

        String computedHash = hashSHA256(stringBuilder.toString());

        if (HASH == null || !HASH.equals(computedHash)) {
            return "Hashes do not match!";
        }

        Payment payment = paymentRepository.findById(Long.parseLong(ID_ZAMOWIENIA)).orElse(null);
        if(payment == null){
            return "Wystąpił nieoczekiwany błąd #3241622!";
        }
        payment.setPaymentId(ID_PLATNOSCI);

        Payment.Status status = Payment.Status.valueOf(STATUS);
        payment.setStatus(status);
        paymentRepository.save(payment);

        if(status != Payment.Status.SUCCESS){
            return STATUS;
        }

        if(payment.getAction() == PaymentAction.PROMOTION_POINTS){
            try{
                PromotionPoints promotionPoints = mapper.readValue(payment.getSuccessAction(), PromotionPoints.class);

                promotionPointService.addPromotionPoints(promotionPoints);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else if(payment.getAction() == PaymentAction.BANNER){
            try{
                Banner banner = mapper.readValue(payment.getSuccessAction(), Banner.class);

                bannerService.active(banner);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return STATUS;
    }
}