package com.blazebit.persistence.testsuite.base;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class SaneMySQLDialect extends MySQL5InnoDBDialect {
	
	@Override
	public String getTableTypeString() {
		return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin";
	}
	
}
