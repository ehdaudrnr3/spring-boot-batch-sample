package com.spring.boot.batch.job.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.spring.boot.batch.model.Customer;

public class CustomerMapper implements RowMapper<Customer> {

	@Override
	public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Customer.builder().id(rs.getLong("id"))
				.firstName(rs.getString("firstName"))
				.lastName(rs.getString("lastName"))
				.birthDate(rs.getString("birthdate"))
				.build();
	}

}
