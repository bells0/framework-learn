# framework-learn
# 第一阶段

## 第二周 分类，推荐，搜索，评价，购物车开发

* 留意一下，枚举类型。很多地方都用到了。写死的数据都要枚举出来

![示意图](Reimg/img.png)

##### 首页轮播图

* 效果图 ![效果图](Reimg/img_1.png)

  点击对应的图片可以跳转。

* 代码实现

  * service层先写接口 ![service](Reimg/img_2.png)

* 然后对应的controller  实现创建indexController，实现carousel接口。注意，这里要自己定义一个枚举

##### 首页分类展示需求  

懒加载机制

> 一级分类

​	

* ![效果图](Reimg/img_3.png)	

* 代码实现：这里用到了一个懒加载机制。主要是前端实现。后端只要获取所有数据
  * service:  ![service](Reimg/img_4.png)

* Controller: ![Kongzhi](Reimg/img_5.png)

> 二级分类

* 首先要通过sql建立多表联合查询，sql代码：

``` sql
SELECT
	f.id as id,
	f.`name` as `name`,
	f.type as type,
	f.father_id as fatherId,
	c.id as subId,
	f.`name` as subName,
	f.type as subType,
	f.father_id as subFatherId
FROM 
	category f
LEFT JOIN
	category c
on
	f.id = c.father_id
where
	f.father_id = 1

```

* service

  调用了上面的sql。功能就是多表联合查询一下，然后将元素返回出来。懒加载机制主要是在前端完成

* controller

  ``` java
      @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类", httpMethod = "GET")
      @GetMapping("/subCat/{rootCatId}")
      public IMOOCJSONResult subCat(
              @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
              @PathVariable Integer rootCatId) {
  
          if (rootCatId == null) {
              return IMOOCJSONResult.errorMsg("分类不存在");
          }
  
          List<CategoryVO> list = categoryService.getSubCatList(rootCatId);
          return IMOOCJSONResult.ok(list);
      }
  ```

  

  

  

  
  
  
  
  
  
  
  
  
  
  
###### 配置自定义mapper

1. 先创建mapper，直接创建接口就行 ![mapper](Reimg/img_6.png)
2. 配置xml文件，绑定并且写sql ![img](Reimg/img_7.png)

 

  

  