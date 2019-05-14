package com.shop.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shop.pojo.Employee;
import com.shop.service.EmployeeService;
import com.shop.utils.Constants;

@Controller
public class EmployeeController {
	@Autowired
	private EmployeeService employeeService;
	
	@RequestMapping(value = "/login")
	public String login(String username,String password,HttpSession session,Model model) {
		Employee employee = this.employeeService.isLogin(username);
		if(employee!=null) {
			if(employee.getPassword().equals(password)) {
				//一定查询到了某个用户
				session.setAttribute(Constants.GLOBLE_USER_SESSION, employee);
				return "index";
			}else {
				model.addAttribute("errorMsg", "账号或密码错误");
				return "login";
			}
		}else {
			model.addAttribute("errorMsg", "账号或密码错误");
			return "login";
		}
	}
	
	@RequestMapping(value = "logout")
	public String logout(HttpSession session) {
		//清除session 信息
		session.invalidate();
		//重定向到login.jsp
		return "redirect:/login.jsp";
	}
}
