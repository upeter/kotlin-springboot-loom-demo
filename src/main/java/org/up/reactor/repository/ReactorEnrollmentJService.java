package org.up.reactor.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class ReactorEnrollmentJService {

    private final static Logger logger = LoggerFactory.getLogger(ReactorAvatarJService.class);
    private final Long delayCfg;
    private final WebClient webClient;

    public ReactorEnrollmentJService(@Value("${remote.service.delay.ms}") Long delayCfg,
                                 @Value("${remote.service.url}") String baseUrl) {
        this.delayCfg = delayCfg;
        webClient = WebClient.create(baseUrl);
    }
    public  Mono<Boolean> verifyEmail(String email) {
        return verifyEmail(email, null);
    }

    public  Mono<Boolean> verifyEmail(String email, Long delay) {
        logger.debug("verify email {}",  email);
        return
                webClient.get()
                        .uri("/echo?email=" + email + "&value=true&delay=" + (delay == null ? delayCfg : delay))
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(Boolean::valueOf);
    }
}




