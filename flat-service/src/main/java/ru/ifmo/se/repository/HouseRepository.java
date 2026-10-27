package ru.ifmo.se.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import ru.ifmo.se.model.House;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
}
