package ru.ifmo.se.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "flat")
@Table(name = "soa_flat")
public class Flat {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Значение поля должно быть больше 0, Значение этого поля должно быть
                     // уникальным, Значение этого поля должно генерироваться автоматически

    @Column(name = "name", nullable = false)
    @Check(constraints = "name <> ''")
    private String name; // Поле не может быть null, Строка не может быть пустой

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates; // Поле не может быть null

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private java.time.LocalDate creationDate; // Поле не может быть null, Значение этого поля должно генерироваться
                                              // автоматически

    @Column(name = "area", nullable = false)
    @Check(constraints = "area > 0 AND area <= 724")
    private int area; // Максимальное значение поля: 724, Значение поля должно быть больше 0

    @Column(name = "number_of_rooms", nullable = false)
    @Check(constraints = "number_of_rooms <= 20 AND number_of_rooms > 0")
    private long numberOfRooms; // Максимальное значение поля: 20, Значение поля должно быть больше 0

    @Enumerated(value = EnumType.STRING)
    @Column(name = "furnish", nullable = true)
    private Furnish furnish; // Поле может быть null

    @Enumerated(value = EnumType.STRING)
    @Column(name = "view", nullable = true)
    private View view; // Поле может быть null

    @Enumerated(value = EnumType.STRING)
    @Column(name = "transport", nullable = true)
    private Transport transport; // Поле может быть null

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = true)
    private House house; // Поле может быть null
}