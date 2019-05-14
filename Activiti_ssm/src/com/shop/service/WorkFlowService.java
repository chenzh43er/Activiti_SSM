package com.shop.service;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import com.shop.pojo.Leavebill;

public interface WorkFlowService {
	void deployProcess(InputStream in,String processName);

	void startProcess(String name,long leaveid);

	List<Task> findTaskListByName(String name);

	Leavebill findLeaveBillByTaskId(String taskId);

	List<Comment> findCommentListByTaskId(String taskId);
	
	//提交批注和完成流程
	void submitTask(long id,String taskId,String comment,String userName);
}
