package org.up.reactor.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.up.coroutines.model.AvatarDto;
import reactor.core.publisher.Mono;

@Component
public class ReactorAvatarJService {

    private final static Logger logger = LoggerFactory.getLogger(ReactorAvatarJService.class);
    private final Long delayCfg;
    private final WebClient webClient;

    public ReactorAvatarJService(@Value("${remote.service.delay.ms}") Long delayCfg,
                                 @Value("${remote.service.url}") String baseUrl) {
        this.delayCfg = delayCfg;
        webClient = WebClient.create(baseUrl);
    }
    public Mono<AvatarDto> randomAvatar() {
        return randomAvatar(null);
    }

    public Mono<AvatarDto> randomAvatar(Long delay) {
        logger.debug("fetch random avatar...");
        return
                webClient.get()
                        .uri("/avatar?delay=" + (delay == null ? delayCfg : delay))
                        .retrieve()
                        .bodyToMono(AvatarDto.class);
    }
}

