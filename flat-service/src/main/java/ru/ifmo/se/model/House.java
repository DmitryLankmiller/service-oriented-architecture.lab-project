package ru.ifmo.se.model;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "house")
@Table(name = "soa_house")
public class House {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = true)
    private String name; // Поле может быть null

    @Column(name = "year", nullable = false, check = @CheckConstraint(constraint = "year > 0 AND year <= 148"))
    private Long year; // Максимальное значение поля: 148, Значение поля должно быть больше 0

    @Column(name = "number_of_flats_on_floor", nullable = false, check = @CheckConstraint(constraint = "number_of_flats_on_floor > 0"))
    private int numberOfFlatsOnFloor; // Значение поля должно быть больше 0
}