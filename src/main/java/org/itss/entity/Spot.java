package org.itss.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spots")
@Getter
@Setter
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String location;
    private Double lat;
    private Double lng;
    private String openTime;
    private String closeTime;
    private boolean isAlwaysOpen;
    private String category;
    private String phone;
    private String pricing;
    private String features;
    private String imageUrl;
}
