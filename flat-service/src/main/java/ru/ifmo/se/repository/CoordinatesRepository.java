package ru.ifmo.se.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import ru.ifmo.se.model.Coordinates;

@Repository 
public interface  CoordinatesRepository extends JpaRepository<Coordinates, Long> {
}
