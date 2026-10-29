package com.example.DATN.service;

import com.example.DATN.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(Order order, String ipAddress) {
        if (ipAddress == null || ipAddress.contains(":")) {
            ipAddress = "127.0.0.1"; // localhost IPv6 sẽ bị VNPay từ chối
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderID() + "T" + System.currentTimeMillis());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderID());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", now.format(fmt));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(fmt));

        String data = buildHashData(params);
        String secureHash = hmacSHA512(hashSecret, data);
        return payUrl + "?" + data + "&vnp_SecureHash=" + secureHash;
    }

    /** Kiểm tra chữ ký VNPay trả về trên return URL */
    public boolean verify(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null) {
            return false;
        }
        Map<String, String> data = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                data.put(k, e.getValue());
            }
        }
        return hmacSHA512(hashSecret, buildHashData(data)).equalsIgnoreCase(received);
    }

    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : new TreeMap<>(params).entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=')
              .append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tạo chữ ký VNPay", e);
        }
    }
}