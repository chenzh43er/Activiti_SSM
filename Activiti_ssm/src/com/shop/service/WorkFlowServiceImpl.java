package com.shop.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;
import com.shop.utils.Constants;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private LeavebillMapper leavebillMapper;	
	//部署流程信息
	@Override
	public void deployProcess(InputStream in, String processName) {
		ZipInputStream inputStream = new ZipInputStream(in);
		this.repositoryService.createDeployment().name(processName)
			.addZipInputStream(inputStream).deploy();
	}

	//启动流程并设置代办人
	@Override
	public void startProcess(String name,long leaveid) {
		Map<String,Object> map = new HashMap<>();
		map.put("userId", name);
		
		String process_key = Constants.Leave_KEY;
		String BUSSINESS_KEY = process_key + "." + leaveid;
		
		//通过key启动流程并设置bussiness_key --> act_hi_procinst
		this.runtimeService.startProcessInstanceByKey(process_key, BUSSINESS_KEY, map);
	}

	//查询当前用户的所有代办事务
	@Override
	public List<Task> findTaskListByName(String name) {
		List<Task> list = taskService.createTaskQuery().
				taskAssignee(name).orderByTaskCreateTime().desc().list();
		return list;
	}

	//通过TaskId 查询LeaveBill 信息
	@Override
	public Leavebill findLeaveBillByTaskId(String taskId) {
		//1.得到任务对象信息(通过taskId 获取Task单例信息)
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		
		//2.从task对象取出流程实例ID 再去查询实例对象
		ProcessInstance processInstance = this.runtimeService
				.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).singleResult();
		
		//3.取出Bussiness_key信息
		String id = "";
		String bussinessKey = processInstance.getBusinessKey();
		
		if(bussinessKey != null && !"".equals(bussinessKey)) {
		//4.从businessKey切割出leaveId
			 id = bussinessKey.split("\\.")[1];
		}
		
		//5.根据leaveId 去查询出请假信息
		Leavebill lb = this.leavebillMapper.selectByPrimaryKey(Long.parseLong(id));
		
		return lb;
	}

	//通过taskId 查询批注信息
	@Override
	public List<Comment> findCommentListByTaskId(String taskId) {
		//1.得到任务对象信息(通过taskId 获取Task单例信息)
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		
		//通过ProcessInstanceId 查询批注列表信息
		List<Comment> list = this.taskService.getProcessInstanceComments(task.getProcessInstanceId());
		System.out.println("task.getProcessInstanceId():"+task.getProcessInstanceId()+",taskId:"+taskId);
		
		
		
		return list;
	}

	@Override
	public void submitTask(long id, String taskId, String comment, String userName) {
		/*添加批注信息*/
		
		//得到任务对象信息(通过taskId 获取Task单例信息)
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		
		//设置当前任务的审核人员
		Authentication.setAuthenticatedUserId(userName);
		
		//添加批注信息
		this.taskService.addComment(taskId, task.getProcessInstanceId(), comment);
		
		/*流程向前推进*/
		this.taskService.complete(taskId);
		
		//获取流程实例
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).singleResult();
		
		if(pi == null) {
			Leavebill leavebill = leavebillMapper.selectByPrimaryKey(id);
			//审批结束 设置为状态2
			leavebill.setState(2);
			leavebillMapper.updateByPrimaryKeySelective(leavebill);
		}
	}

}
