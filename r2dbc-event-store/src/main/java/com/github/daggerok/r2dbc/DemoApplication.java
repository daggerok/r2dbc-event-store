package com.github.daggerok.r2dbc;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

interface Identity<NUMBER> {
	NUMBER getSequenceNumber();
}

interface DomainEvent<IDENTITY> {
	IDENTITY getAggregateId();
}

interface HistoricallyTrackable<TIME> {
	TIME getOccurredAt();
}

@Data
@Table("domain_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED, onConstructor_ = @PersistenceConstructor)
class DenormalizedEvent implements Identity<Long>, DomainEvent<UUID>, HistoricallyTrackable<LocalDateTime> {

	/* common event fields: */
	private String eventType;
	@Id
	@Setter(AccessLevel.PUBLIC)
	private Long sequenceNumber;
	private UUID aggregateId;
	@Setter(AccessLevel.PUBLIC)
	private LocalDateTime occurredAt;

	/* register visitor event fields: */
	private String name;
	private LocalDateTime expireAt;

	/* pass card delivered event fields: none */

	/* door entered event fields: */
	private String doorId;
}

@Data
@Getter
@Table("domain_events")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class VisitorRegisteredEvent extends DenormalizedEvent {

	public static VisitorRegisteredEvent of(UUID aggregateId, String name, LocalDateTime expireAt) {
		return new VisitorRegisteredEvent(aggregateId, name, expireAt);
	}

	VisitorRegisteredEvent(UUID aggregateId, String name, LocalDateTime expireAt) {
		super(VisitorRegisteredEvent.class.getSimpleName(),
					null, aggregateId, LocalDateTime.now(),
					name, expireAt, null);
	}
}

@Data
@Getter
@Table("domain_events")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
// @AllArgsConstructor(onConstructor_ = @PersistenceConstructor)
class PassCardDeliveredEvent extends DenormalizedEvent {

	public static PassCardDeliveredEvent of(UUID aggregateId) {
		return new PassCardDeliveredEvent(aggregateId);
	}

	PassCardDeliveredEvent(UUID aggregateId) {
		super(PassCardDeliveredEvent.class.getSimpleName(),
					null, aggregateId, LocalDateTime.now(),
					null, null, null);
	}
}

@Data
@Getter
@Table("domain_events")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class EnteredTheDoorEvent extends DenormalizedEvent {

	public static EnteredTheDoorEvent of(UUID aggregateId, String doorId) {
		return new EnteredTheDoorEvent(aggregateId, doorId);
	}

	EnteredTheDoorEvent(UUID aggregateId, String doorId) {
		super(EnteredTheDoorEvent.class.getSimpleName(),
					null, aggregateId, LocalDateTime.now(),
					null, null, doorId);
	}
}

interface EventStore extends ReactiveCrudRepository<DenormalizedEvent, Long> {

	// @Query(" select sequence_number,						" +
	// 		"					  aggregate_id,								" +
	// 		"					  occurred_at,								" +
	// 		"					  name,												" +
	// 		"					  expire_at,									" +
	// 		"					  door_id,										" +
	// 		" 		 from domain_events 							" +
	// 		" 	  where aggregate_id = :aggregateId " +
	// 		"  order_by sequence_number ASC 				")
	Flux<DenormalizedEvent> findByAggregateIdOrderByAggregateIdAsc(UUID aggregateId);
}

interface MutableState {
	MutableState mutate(DenormalizedEvent domainEvent);
}

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Infrastructure {

	public static final Function<Throwable, Map> wrap =
			throwable -> Map.of("error", throwable.getMessage());

	public static final Function<String, Supplier<RuntimeException>> error =
			message -> () -> new RuntimeException(message);
}

@Data
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
class VisitorState implements MutableState, Identity<Long>, DomainEvent<UUID>, HistoricallyTrackable<LocalDateTime> {

	private Long sequenceNumber;
	private UUID aggregateId;
	private LocalDateTime occurredAt;

	private String name;
	private LocalDateTime expireAt;

	private LocalDateTime deliveredAt;

	private String lastDoorId;
	private LocalDateTime lastDoorEnteredAt;

	@Override
	public VisitorState mutate(DenormalizedEvent domainEvent) {
		var anEvent = Optional.ofNullable(domainEvent)
													.orElseThrow(Infrastructure.error.apply("Domain event may not be null"));
		if (anEvent instanceof VisitorRegisteredEvent) return onVisitorRegisteredEvent((VisitorRegisteredEvent) anEvent);
		if (anEvent instanceof PassCardDeliveredEvent) return onPassCardDeliveredEvent((PassCardDeliveredEvent) anEvent);
		if (anEvent instanceof EnteredTheDoorEvent) return onEnteredTheDoorEvent((EnteredTheDoorEvent) anEvent);
		return onUnsupportedDomainEvent(anEvent);
	}

	private VisitorState onVisitorRegisteredEvent(VisitorRegisteredEvent event) {
		return this.setAggregateId(event.getAggregateId())
							 .setName(event.getName())
							 .setExpireAt(event.getExpireAt())
							 .setSequenceNumber(event.getSequenceNumber())
							 .setOccurredAt(event.getOccurredAt());
	}

	private VisitorState onPassCardDeliveredEvent(PassCardDeliveredEvent event) {
		return this.setDeliveredAt(event.getOccurredAt())
							 .setSequenceNumber(event.getSequenceNumber())
							 .setOccurredAt(event.getOccurredAt());
	}

	private VisitorState onEnteredTheDoorEvent(EnteredTheDoorEvent event) {
		return this.setLastDoorId(event.getDoorId())
							 .setLastDoorEnteredAt(event.getOccurredAt())
							 .setSequenceNumber(event.getSequenceNumber())
							 .setOccurredAt(event.getOccurredAt());
	}

	private VisitorState onUnsupportedDomainEvent(DomainEvent<UUID> event) {
		log.warn("Fallback: {}", event);
		return this;
	}
}

@Data
@Getter
@Table("snapshots")
@ToString(callSuper = true)
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class StateSnapshot extends VisitorState {

	@Id
	private Long id;

	@Version
	private Long version;

	@PersistenceConstructor
	StateSnapshot(Long id, Long version, Long sequenceNumber, UUID aggregateId, LocalDateTime occurredAt,
								String name, LocalDateTime expireAt, LocalDateTime deliveredAt,
								String lastDoorId, LocalDateTime lastDoorEnteredAt) {
		super(sequenceNumber, aggregateId, occurredAt, name, expireAt, deliveredAt, lastDoorId, lastDoorEnteredAt);
		this.id = id;
		this.version = version;
	}

	public StateSnapshot patchWith(VisitorState state) {

		Optional.ofNullable(state.getSequenceNumber())
						.filter(that -> !that.equals(this.getSequenceNumber()))
						.ifPresent(this::setSequenceNumber);

		Optional.ofNullable(state.getAggregateId())
						.filter(that -> !that.equals(this.getAggregateId()))
						.ifPresent(this::setAggregateId);

		Optional.ofNullable(state.getOccurredAt())
						.filter(that -> !that.equals(this.getOccurredAt()))
						.ifPresent(this::setOccurredAt);

		Optional.ofNullable(state.getName())
						.filter(that -> !that.equals(this.getName()))
						.ifPresent(this::setName);

		Optional.ofNullable(state.getExpireAt())
						.filter(that -> !that.equals(this.getExpireAt()))
						.ifPresent(this::setExpireAt);

		Optional.ofNullable(state.getDeliveredAt())
						.filter(that -> !that.equals(this.getDeliveredAt()))
						.ifPresent(this::setDeliveredAt);

		Optional.ofNullable(state.getLastDoorId())
						.filter(that -> !that.equals(this.getLastDoorId()))
						.ifPresent(this::setLastDoorId);

		Optional.ofNullable(state.getLastDoorEnteredAt())
						.filter(that -> !that.equals(this.getLastDoorEnteredAt()))
						.ifPresent(this::setLastDoorEnteredAt);

		return this;
	}

	public static StateSnapshot noop(UUID aggregateId) {
		return new StateSnapshot(null, null, 0L, aggregateId, null, null, null, null, null, null);
	}

	/* NOTE: this method is package-private */ static StateSnapshot from(VisitorState state) {
		return new StateSnapshot(null, null, state.getSequenceNumber(), state.getAggregateId(), state.getOccurredAt(),
														 state.getName(), state.getExpireAt(), state.getDeliveredAt(),
														 state.getLastDoorId(), state.getLastDoorEnteredAt());
	}
}

interface SnapshotStore extends ReactiveCrudRepository<StateSnapshot, Long> { }

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
