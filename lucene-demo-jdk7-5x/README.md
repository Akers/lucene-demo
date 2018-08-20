# 样例运行
## 导入项目
项目基于Maven，直接导入maven项目即可
### eclipse
File -> Import... -> Existing Maven Projects
### idea
File -> Open... 选择Demo里的pom.xml -> 点击 Open as Project
## Runtime设置
项目jdk版本使用1.7版本

## 运行项目
### eclipse
eclipse先安装STS插件：Eclipse Marketplace里搜STS，或者https://spring.io/tools/sts 下载到本地后安装
安装后，右键com.sinosoftgz.demo.lucenedemo.LuceneDemoApplication.java，弹出菜单选 Run As... -> Spring Boot App
### idea
Idea导入项目后，右上角点运行即可运行，如右上角运行配置中未自行加载启动配置，可打开com.sinosoftgz.demo.lucenedemo.LuceneDemoApplication.java文件，点击类名上的绿色箭头即可启动：


## 样例说明
访问http://localhost:8081/lucene-demo/init 初始化索引数据
访问http://localhost:8081/lucene-demo/ 可以看Demo实现效果（主要是全文搜索、精确匹配、日期（数值）范围查找等几个常用的检索）

## 配置文件说明
重点配置文件，主要需要配置以下几个配置：
1. applications.yml
```yml
lucene: 
  index-path: 'D:/lucenedemo/index'
  backup-path: 'D:/lucenedemo/index_backup'
  bufferSize: 256
```
配置解析：
lucene
bufferSize: 生成索引时的内存缓存大小，当lucene占用的内存到达这个值后，会自动的flush到磁盘，理论上bufferSize越大生成大量索引时速度越快
index-path: 索引文件的路径
backup-path 索引文件的备份路径

2. jcseg.properties
此配置文件为jcseg分词器的配置文件，一般简单的搜索都不需要改动，只需配置
lexicon.path指向项目所用的词典：src/main/resources/lexicon

# 样例代码介绍

## SmsDocIndexGenerator

SmsDocIndexGenerator类主要实现通过数据生成索引的封装，其中以SmsDoc作为样例数据结构

## SmsDocSearcher
SmsDocSearcher类主要演示如何对索引进行搜索

## LuceneIndexUtils 和 LuceneSearchUtils

LuceneIndexUtils 封装Lucene索引操作的工具类，实现了索引创建、删除等常用功能
LuceneSearchUtils 封装Lucene索引搜索的工具类
代码可直接拷贝使用，基于Lucene 5.X版本实现
**复用时注意：**
项目中使用了Spring的@Value注解，如不使用SpringBoot，需配置好配置文件读取，spring xml配置例子：
```xml
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
     <property name="locations">
     <list>
         <value>classpath:applications.properties</value>
     </list>
        </property>
</bean>
<context:annotation-config/>
```
然后在properties文件中配置以下配置项
lucene.index-path=索引文件的路径
lucene.backup-path=索引文件的备份路径
lucene.bufferSize=Buffer内存最大值

如不想采用配置文件，需自行配置Bean、删除@Compoment及@Value注解并将相关配置注入
```xml
<bean id="luceneIndexUtils" class="com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneIndexUtils">
   <property name="luceneIndexPath" value="D:/lucenedemo/index"></property>
   <property name="backupIndexDir" value="D:/lucenedemo/index_backup"></property>
   <property name="bufferSize" value="256"></property>
</bean>

<bean id="luceneSearchUtils" class="com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneSearchUtils">
   <property name="luceneIndexPath" value="D:/lucenedemo/index"></property>
   <property name="bufferSize" value="256"></property>
</bean>
```
