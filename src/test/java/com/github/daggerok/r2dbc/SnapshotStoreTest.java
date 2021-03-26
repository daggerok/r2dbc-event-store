package com.github.daggerok.r2dbc;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.util.UUID;

@Log4j2
@SpringBootTest
@DisplayName("A snapshot store tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SnapshotStoreTest {

  @Autowired
  SnapshotStore snapshotStore;

  @BeforeEach
  void setUp() {
    snapshotStore.deleteAll().subscribe(log::info);
  }

  @Test
  void should_find_all() {
    StepVerifier.create(snapshotStore.findAll()) // .consumeNextWith(log::info)
                .verifyComplete();
  }

  @Test
  void should_save() {
    StepVerifier.create(snapshotStore.save(StateSnapshot.noop(UUID.randomUUID())))
                .consumeNextWith(log::info)
                .verifyComplete();
  }
}
