package org.up.reactor;

import jakarta.transaction.Transactional;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.up.coroutines.model.User;
import org.up.reactor.repository.ReactorUserJRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class ReactorUserJRepositoryTest {

    @Autowired
    ReactorUserJRepository reactorUserRepository;

    @Test
    public void shouldInsertNewUserAndFindItByUserName() {
        User newUser = new User(null, "john", "joe@home.nl", false, null);
        reactorUserRepository.save(newUser)
                .as(StepVerifier::create)
                .assertNext(i -> assertNotEquals(i.getId(), null))
                .verifyComplete();

        reactorUserRepository.findByUserName(newUser.getUserName())
                .as(StepVerifier::create)
                .assertNext(u -> MatcherAssert.assertThat(u.getUserName(), is("john")))
                .verifyComplete();
    }
}
