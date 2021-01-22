package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "注册登录",tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("/passport")   //代表这个类下面的所有请求全都要走/passport这儿
public class PassportController {
    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户名是否存在",notes ="用户名是否存在",httpMethod ="GET" )
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
    @ApiOperation(value = "用户注册",notes ="用户注册",httpMethod ="POST" )
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,
                                  HttpServletRequest request,
                                  HttpServletResponse response)//requestbody接收数据
    {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassWord = userBO.getConfirmPassword();

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
        Users userResult=userService.createUser(userBO);

        userResult = setNullProperty(userResult);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 生成用户token，存入redis会话
        // TODO 同步购物车数据
        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        // 1. 实现登录
        Users userResult = userService.queryUserForLogin(username,  //这里是明文，要加密
                MD5Utils.getMD5Str(password));   //加密操作

        if (userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }


        userResult = setNullProperty(userResult);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);


        // TODO 生成用户token，存入redis会话
        // TODO 同步购物车数据

        return IMOOCJSONResult.ok(userResult);
    }

    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
}
