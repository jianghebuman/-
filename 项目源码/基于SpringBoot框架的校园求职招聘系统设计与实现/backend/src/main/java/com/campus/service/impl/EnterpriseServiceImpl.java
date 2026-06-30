package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.Enterprise;
import com.campus.mapper.EnterpriseMapper;
import com.campus.service.EnterpriseService;
import org.springframework.stereotype.Service;

/**
 * 企业服务实现
 *
 * @author campus
 */
@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseService {
}
