package ru.ifmo.se.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.model.House;

@Repository 
public interface HouseRepository extends JpaRepository<House, Long> {
}
