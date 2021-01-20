package com.imooc.service.impl;

import com.imooc.enums.Sex;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UsersMapper usersMapper;
    @Autowired
    private Sid sid;   //这个要扫描到，启动类里要添加配置 component注入

    private static final String USER_FACE="http://wonderbell.cn/img/index.jpg";

    //注册中使用，判断用户名是否存在
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {//查询条件
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        Users result = usersMapper.selectOneByExample(example);
        return result==null ? false : true;
    }


    //创建用户
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users createUser(UserBO userBO) {  //try catch 快捷键：ctrl+alt+t
        String userId = sid.nextShort();
        Users users = new Users();
        users.setId(userId);
        users.setUsername(userBO.getUsername());
        try {
            users.setPassword(MD5Utils.getMD5Str(userBO.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //默认用户昵称同用户名
        users.setNickname(userBO.getUsername());
        //默认头像
        users.setFace(USER_FACE);
        //设置默认生日
        users.setBirthday(DateUtil.stringToDate("2021-01-01"));   //这个要引入这个工具类
        //默认性别为 保密
        users.setSex(Sex.secret.type);   //这里推荐使用枚举
        users.setCreatedTime(new Date());
        users.setUpdatedTime(new Date());
        usersMapper.insert(users);
        return users;
    }
}
