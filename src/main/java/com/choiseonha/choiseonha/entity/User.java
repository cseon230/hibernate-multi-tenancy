package com.choiseonha.choiseonha.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USER")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Integer userNo;

    @Column(name = "USER_ID")
    private String  userId;

    @Column(name = "USER_PWD")
    private String userPwd;
}
