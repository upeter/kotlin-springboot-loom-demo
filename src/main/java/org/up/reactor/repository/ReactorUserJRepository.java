package org.up.reactor.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.up.coroutines.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactorUserJRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByUserName(String name);

    Flux<User> findById_GreaterThan(Long id);

}