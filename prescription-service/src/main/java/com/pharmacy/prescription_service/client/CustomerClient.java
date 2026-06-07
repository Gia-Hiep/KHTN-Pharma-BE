package com.pharmacy.prescription_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CustomerClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${customer.base-url}")
    private String baseUrl;

    public CustomerDto getByPhone(String bearerToken, String phone) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        HttpEntity<Void> req = new HttpEntity<>(h);

        try {
            ResponseEntity<CustomerDto> res = rest.exchange(
                    baseUrl + "/customers/phone/" + phone,
                    HttpMethod.GET,
                    req,
                    CustomerDto.class
            );
            return res.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public CustomerDto create(String bearerToken, CreateCustomerDto body) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CustomerDto> res = rest.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                CustomerDto.class
        );
        return res.getBody();
    }

    public record CustomerDto(Long id, String fullName, String phone) {}
    public record CreateCustomerDto(String fullName, String phone, String email, String address,
                                    String gender, String notes) {}
}
