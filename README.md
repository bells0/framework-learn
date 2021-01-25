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
  * 前面也要自定义mapper。跟下方 懒加载机制实现一样
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

 ##### 下拉懒加载

下方具体的展示随着鼠标下拉，懒加载，进行展示。

* 多表关联查询的sql:

``` sql
SELECT
f.id as rootCatId,
f.`name` as rootCatName,
f.slogan as slogan,
f.cat_image as catImage,
f.bg_color as bgColor,
i.id as itmeId,
i.item_name as itemName,
ii.url as itemUrl,
i.created_time as createdTime
FROM
	category f
LEFT JOIN
	items i
ON
	f.id = i.root_cat_id
LEFT JOIN
	items_img ii
ON
	i.id = ii.item_id
WHERE
	f.type= 1
AND
	i.root_cat_id = 7
AND
	ii.is_main= 1
ORDER BY
	i.created_time
DESC
LIMIT 0,6
```



* 自定义Mapper

  ![img](Reimg/img_8.png)

  xml文件配置：

  ```xml
      <resultMap id="myNewItemsVO" type="com.imooc.pojo.vo.NewItemsVO">
          <id column="rootCatId" property="rootCatId"/>
          <result column="rootCatName" property="rootCatName"/>
          <result column="slogan" property="slogan"/>
          <result column="catImage" property="catImage"/>
          <result column="bgColor" property="bgColor"/>
  
          <collection property="simpleItemList" ofType="com.imooc.pojo.vo.SimpleItemVO">
              <id column="itemId" property="itemId"/>
              <result column="itemName" property="itemName"/>
              <result column="itemUrl" property="itemUrl"/>
          </collection>
      </resultMap>
  
      <select id="getSixNewItemsLazy" resultMap="myNewItemsVO" parameterType="Map">
          SELECT
              f.id as rootCatId,
              f.`name` as rootCatName,
              f.slogan as slogan,
              f.cat_image as catImage,
              f.bg_color as bgColor,
              i.id as itemId,
              i.item_name as itemName,
              ii.url as itemUrl,
              i.created_time as createdTime
          FROM
              category f
          LEFT JOIN items i ON f.id = i.root_cat_id
          LEFT JOIN items_img ii ON i.id = ii.item_id
          WHERE
              f.type = 1
          AND
              i.root_cat_id = #{paramsMap.rootCatId}
          AND
              ii.is_main = 1
          ORDER BY
              i.created_time
          DESC
          LIMIT 0,6
      </select>
  
  ```

  * 添加vo：

  ``` java
  public class NewItemsVO {
  
      private Integer rootCatId;
      private String rootCatName;
      private String slogan;
      private String catImage;
      private String bgColor;
  
      private List<SimpleItemVO> simpleItemList;}  
  //省略get与set方法
  ```

  

* service

  接口：   

  ```java
      /**
       * 查询首页每个一级分类下的6条最新商品数据
       * @param rootCatId
       * @return
       */
      public List<NewItemsVO> getSixNewItemsLazy(Integer rootCatId);
  
  }
  ```

  实现：

  ```java
      @Transactional(propagation = Propagation.SUPPORTS)
      @Override
      public List<NewItemsVO> getSixNewItemsLazy(Integer rootCatId) {
          Map<String, Object> map = new HashMap<>();
          map.put("rootCatId", rootCatId);
          return categoryMapperCustom.getSixNewItemsLazy(map);
      }
  ```

* controller:

  ``` java
      @ApiOperation(value = "查询每个一级分类下的最新6条商品数据", notes = "查询每个一级分类下的最新6条商品数据", httpMethod = "GET")
      @GetMapping("/sixNewItems/{rootCatId}")
      public IMOOCJSONResult sixNewItems(
              @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
              @PathVariable Integer rootCatId) {
  
          if (rootCatId == null) {
              return IMOOCJSONResult.errorMsg("分类不存在");
          }
  
          List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
          return IMOOCJSONResult.ok(list);
      }
  ```

  

  

  