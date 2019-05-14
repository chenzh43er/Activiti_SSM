package com.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;

@Service
public class LeaveBillServiceImpl implements LeaveBillService{

	@Autowired
	private LeavebillMapper leavebillMapper;
	
	@Override
	public void saveLeaveBill(Leavebill leavebill) {
		this.leavebillMapper.insert(leavebill);
	}
	
}
