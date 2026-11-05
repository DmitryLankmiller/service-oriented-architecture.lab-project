package ru.ifmo.se.repository;

import java.util.List;
import java.util.Optional;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.View;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long>, JpaSpecificationExecutor<Flat> {
    Optional<Flat> findFirstByNumberOfRooms(int numberOfRooms);
    List<Flat> findByViewInList(List<View> views);
}
