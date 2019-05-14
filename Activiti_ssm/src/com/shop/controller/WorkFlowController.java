package com.shop.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.shop.pojo.Employee;
import com.shop.pojo.Leavebill;
import com.shop.service.EmployeeService;
import com.shop.service.LeaveBillService;
import com.shop.service.WorkFlowService;
import com.shop.utils.Constants;

@Controller
public class WorkFlowController {
	
	@Autowired
	private WorkFlowService workFlowService;
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private LeaveBillService leaveBillService;
	
	@RequestMapping(value = "/deployProcess")
	public String deployProcess(MultipartFile fileName,String processName) throws IOException {
		/**
		 * 部署流程
		 */
		this.workFlowService.deployProcess(fileName.getInputStream(), processName);
		return "add_process";
	}
	
	@RequestMapping(value = "/saveStartLeave")
	public String saveStartLeave(Leavebill leavebill,HttpSession session) {
		/**
		 * 1.将请假业务信息插入到leaveBill表中
		 * 
		 * 2.启动打枊前流程
		 */
		leavebill.setLeavedate(new Date());
		
		/**
		 * 1.表示当前流程正在运行
		 * 
		 * 2.表示当前流程已经全部结束
		 */
		leavebill.setState(2);
		
		Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
		
		leavebill.setUserId(employee.getId());
		
		this.leaveBillService.saveLeaveBill(leavebill);
		
		//启动流程
		long leaveid = leavebill.getId();
		
		this.workFlowService.startProcess(employee.getName(),leaveid);
		
		return "redirect:/taskList";
		
	}
	
	@RequestMapping(value = "taskList")
	public String getTaskList(HttpSession session,Model model) {
		Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
		/**
		 * 查询当前登录人的代办事务信息
		 */
		List<Task> list = this.workFlowService.findTaskListByName(employee.getName());
		model.addAttribute("taskList", list);
		return "workflow_task";
	}
	
	@RequestMapping(value = "/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		
		//根据taskId 查询出 leaveBill
		Leavebill leavebill = this.workFlowService.findLeaveBillByTaskId(taskId);
		
		//根据taskId 查询出批注列表信息
		List<Comment> list = this.workFlowService.findCommentListByTaskId(taskId);
		
		for (Comment comment : list) {
			System.out.println(comment.getFullMessage());
		}
		
		ModelAndView mv = new ModelAndView();
		
		mv.addObject("taskId",taskId);
		
		mv.addObject("bill",leavebill);
		
		mv.addObject("id",leavebill.getId());
		
		mv.addObject("commentList",list);
		
		mv.setViewName("approve_leave");
		
		return mv;
	}
	
	@RequestMapping(value = "/submitTask")
	public String submitTask(long id,String taskId,String comment,HttpSession session) {
		
		Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
		
		this.workFlowService.submitTask(id, taskId, comment, employee.getName());
		
		return "redirect:/taskList";
	}
}
