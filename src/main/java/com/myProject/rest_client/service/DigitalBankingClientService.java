package com.myProject.rest_client.service;

import com.myProject.rest_client.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DigitalBankingClientService {

    private final RestClient restClient;

    public List<AccountResponse> getMyAccounts() {
        String token = getToken();
        return restClient.get()
                .uri("accounts/my-accounts")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountResponse>>(){});
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        String token = getToken();
        return restClient.get()
                .uri("/accounts/{accountNumber}", accountNumber)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(AccountResponse.class);
    }

    public void withdraw(TransactionRequest request) {
        String token = getToken();
        restClient.post()
                .uri("/transactions/withdraw")
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void deposit(TransactionRequest request) {
        String token = getToken();
        restClient.post()
                .uri("/transactions/deposit")
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private String getToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        throw new AuthenticationCredentialsNotFoundException("Token not found");
    }

}
