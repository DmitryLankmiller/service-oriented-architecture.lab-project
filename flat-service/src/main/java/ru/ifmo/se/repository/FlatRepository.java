package ru.ifmo.se.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import ru.ifmo.se.model.Flat;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long>, JpaSpecificationExecutor<Flat> {
}
