package com.spring.boot.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.boot.batch.entity.Pay;

public interface PayRepository extends JpaRepository<Pay, Long>{

}
