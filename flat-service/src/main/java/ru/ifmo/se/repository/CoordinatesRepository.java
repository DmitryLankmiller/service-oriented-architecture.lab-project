package ru.ifmo.se.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.model.Coordinates;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {
}
