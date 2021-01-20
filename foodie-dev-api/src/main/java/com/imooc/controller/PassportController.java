package com.imooc.controller;

import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.IMOOCJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passport")   //代表这个类下面的所有请求全都要走/passport这儿
public class PassportController {
    @Autowired
    private UserService userService;
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username){ //@RequestParam 表示是一种请求类型的参数，而不是目的类型的参数
        //1.判断用户名不能为空
        if(StringUtils.isBlank(username)){
           // return HttpStatus.INTERNAL_SERVER_ERROR;//这个是为了返回对应的状态码，HttpStatus这里点进去可以看到对应的参数
            //使用自定义状态数据结构：      IMOOCJSONResult.errorMsg("用户名不能为空");
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        //2、查找注册的用户名是否存在
        boolean b = userService.queryUsernameIsExist(username);
        System.out.println(b);
        if (b){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //3.请求成功，用户名没有重复
        return IMOOCJSONResult.ok();
    }

    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO)//requestbody接收数据
    {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassWord = userBO.getConfirmPassWord();

        //0.判断用户名和密码必不为空
        if(StringUtils.isBlank(username)||
        StringUtils.isBlank(password)||
        StringUtils.isBlank(confirmPassWord)){

            return IMOOCJSONResult.errorMsg("用户名或密码为空");
        }
        //1.查询用户名是否存在
        boolean b = userService.queryUsernameIsExist(username);
        if (b){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //2.判断密码长度不能少于6位
        if(password.length()<6){
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }
        //3.判断两次密码是否一致
        if (!password.equals(confirmPassWord)){
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        //4.实现注册
        userService.createUser(userBO);

        return IMOOCJSONResult.ok();
    }

}
