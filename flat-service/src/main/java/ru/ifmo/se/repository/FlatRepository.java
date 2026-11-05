package ru.ifmo.se.repository;

import java.util.List;
import java.util.Optional;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.View;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long>, JpaSpecificationExecutor<Flat> {
    Optional<Flat> findFirstByNumberOfRooms(int numberOfRooms);

    List<Flat> findByViewInList(List<View> views);

    long countByHouseIdIsNull();

    @Query("""
        SELECT COUNT(f)
        FROM flat f JOIN f.house h
        WHERE (h.name = :name OR (h.name IS NULL AND :name IS NULL))
            AND h.year = :year
            AND h.numberOfFlatsOnFloor = :numberOfFlatsOnFloor
        """)
    long countByHouse(@Nullable String name, long year, int numberOfFlatsOnFloor);
}
