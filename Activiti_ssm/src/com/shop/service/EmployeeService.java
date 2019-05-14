package com.shop.service;

import com.shop.pojo.Employee;
import com.shop.pojo.Leavebill;

public interface EmployeeService {
	Employee isLogin(String username);
	
	Employee findEmployeeManagerByManagerId(long managerId);
}
