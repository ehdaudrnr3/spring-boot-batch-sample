package com.spring.boot.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.boot.batch.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{

}
