package com.shop.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shop.pojo.Employee;
import com.shop.service.EmployeeService;

public class CustomerTaskAssignee implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		//拿到spring容器
		WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();

		EmployeeService emService = (EmployeeService) webApplicationContext.getBean("employeeService");
		
		//调用服务层中的方法根据managerId 查询
		// java普通类中获取request对象
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
						.getRequest();
		
		HttpSession session = request.getSession();
		
		Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
		
		//调用服务层中的方法根据managerId，查询出当前代办人的上级
		Employee manager = emService.findEmployeeManagerByManagerId(employee.getManagerId());
		
		//通过监听器设置代办人
		delegateTask.setAssignee(manager.getName());
	}
}
