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

#### 搜索 查看商品详情

* 效果：![效果](Reimg/img_9.png)

  可以展示出图片价格，口味选项等信息

* 代码实现：

  * controller层 ItemsController: 

  ```java
  @ApiOperation(value = "查询商品详情", notes = "查询商品详情", httpMethod = "GET")
      @GetMapping("/info/{itemId}")
      public IMOOCJSONResult info(
              @ApiParam(name = "itemId", value = "商品id", required = true)
              @PathVariable String itemId) {
  
          if (StringUtils.isBlank(itemId)) {
              return IMOOCJSONResult.errorMsg(null);
          }
  
          Items item = itemService.queryItemById(itemId);
          List<ItemsImg> itemImgList = itemService.queryItemImgList(itemId);
          List<ItemsSpec> itemsSpecList = itemService.queryItemSpecList(itemId);
          ItemsParam itemsParam = itemService.queryItemParam(itemId);
                  //这里有四个要传递的参数，但是json每次只能传递一个，因此要借助VO
          ItemInfoVO itemInfoVO = new ItemInfoVO();
          itemInfoVO.setItem(item);
          itemInfoVO.setItemImgList(itemImgList);
          itemInfoVO.setItemSpecList(itemsSpecList);
          itemInfoVO.setItemParams(itemsParam);
          return IMOOCJSONResult.ok(itemInfoVO);
      }
  ```

  service层代码就是查询，这里忽略。

  

#### 商品评价展示并且实现分页

商品展示也涉及到多表的连接查询，要自定义sql。  
* ItemsMapperCustom：

```java
 public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String, Object> map);
```

* xml实现：

```xml
  <select id="queryItemComments" parameterType="Map" resultType="com.imooc.pojo.vo.ItemCommentVO">
    SELECT
        ic.comment_level as commentLevel,
        ic.content as content,
        ic.sepc_name as specName,
        ic.created_time as createdTime,
        u.face as userFace,
        u.nickname as nickname
    FROM
        items_comments ic
    LEFT JOIN
        users u
    ON
        ic.user_id = u.id
    WHERE
        ic.item_id = #{paramsMap.itemId}
        <if test=" paramsMap.level != null and paramsMap.level != '' ">
          AND ic.comment_level = #{paramsMap.level}
        </if>
  </select>

```

对比一下和前面的下拉懒加载的xml实现，为何这里没有<resultMap>?

  因为这里的resultType是直接从pojo中定义了，所以上面不需要再定义<resultMap> 。所以，这里的数据全是放在了VO中的

* service层实现

###### 分页使用mybatis分页插件.
 使用分页插件是在service中使用。

PageHelper.startPage(page, pageSize);

```xml
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>1.2.12</version>
        </dependency>
```

* 先来编写service:

``` java
  @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedComments(String itemId,
                                                  Integer level,
                                                  Integer page,
                                                  Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("level", level);

        // mybatis-pagehelper

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);

        List<ItemCommentVO> list = itemsMapperCustom.queryItemComments(map);
//        System.out.println(list);
        for (ItemCommentVO vo : list) {
            vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));
        }

        return setterPagedGrid(list, page);
    }
    private PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }
```
* 为分页设置一个数据格式：

```java
	public class PagedGridResult {
	
	private int page;			// 当前页数
	private int total;			// 总页数	
	private long records;		// 总记录数
	private List<?> rows;		// 每行显示的内容
	}  //省略set与get方法
	```
	
	* controller:
```java

 @ApiOperation(value = "查询商品评论", notes = "查询商品评论", httpMethod = "GET")
    @GetMapping("/comments")
    public IMOOCJSONResult comments(
            @ApiParam(name = "itemId", value = "商品id", required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level", value = "评价等级", required = false)
            @RequestParam(required = false) Integer level,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize) {

        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;  //这里通用化一些，COMMON这个统一放一起
        }
        System.out.println(level);
        PagedGridResult grid = itemService.queryPagedComments(itemId,
                                                                level,
                                                                page,
                                                                pageSize);
        return IMOOCJSONResult.ok(grid);
    }
```

###### 注意坑

查询全部评论时，level应该是为空，但是会返回空白页面。这里设置参数  **required=false** 来解决。表示level为非必须参数。

