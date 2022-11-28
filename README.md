# 云计算：面向StackOverflow的问题标签统计

## 项目说明

### 作业说明

本项目为2022年秋云计算课程第一次作业，主要内容为基于Spark和Flink的流计算应用。代码各部分内容与目录名称对应。

### 应用简介

本项目爬取了StackOverflow网站近五年（2018年至今）的问题数据，包含标签、回答数、浏览量等具体词条，分析了各项因子对标签热度的影响，计算了不同标签在出现频数与热度等方面的情况对比，以及部分标签随时间的变化关系，最后借助词云和动态条形图分别展示了相关计算结果。

### 成员分工

- 522022320003 曹英瑞 前端
- 522022320004 陈泔錞 Flink
- 522022320047 何文兵 数据爬取
- 522022320092 刘一铭 Spark

## 数据获取说明

编写python脚本（在data目录下），使用四台服务器并行爬取StackOverFlow上的问题列表（按问题提出的时间进行排序）。

总共搜集了近五年来总共800多万个问题。

脚本利用requests_html库向目标网站发送http请求，从而获取网页的html文件，通过CSS selector定位需要的页面元素内容，解析出需要的数据，形成csv文件，以供后续使用

数据格式：

questionID,questionName,timeStamp,votes,answers,views,tags

其中tags列中的多个标签用;隔开

### 如何防止被网站的反爬虫机制捕捉？

- 修改请求头，伪装为浏览器

- 每获取1W条数据爬虫暂停5min

### 爬虫如何保证数据不重复？

- 记录每一页最后一个问题的questionID，获取下一页时，由于newest列表在前方更新了新提出的问题，所以向后选取，直至选取的问题questionID小于之前记录的最后一个questionID

### 数据什么时候落盘形成csv文件？

- 爬虫暂停时进行数据落盘，清空缓冲区
- 按年-月记录数据，当这个月的数据爬取完毕时，创建新文件，同时进行数据落盘，清空缓冲区

## Flink项目

- Flink项目使用Scala语言开发，分别对爬取的数据中的tags部分进行了WordCount和加权处理WeightCount
- 运行方式如下：

1. Flink在项目中打包生成jar文件
2. 将jar文件上传至master服务器的/usr/local文件夹下
3. master下命令行：
   - cd /usr/local/flink
   - bin/flink run ../Flink-1.0-SNAPSHOT.jar
4. 执行上述命令行后，会对已经上传在服务器上的数据进行处理并在/data/outCount/和/data/outWeight/文件夹下输出结果文件供前端使用


## Spark项目

- spark 在项目中打包生成jar文件
- 将jar文件上传至服务器
- 在服务器中以spark submit方式运行jar包
- 监听hdfs文件夹/input_data
- 每当有文件上传至hdfs中则会对其进行处理生成txt文件保存并供前端使用

### 项目实现功能

- 将爬取的数据从监听的hdfs文件夹中获取到
- 处理数据并计算Tag出现次数
- 将Tag按次数排序并返回前30
- 将前30的Tag保存为文件


## 前端

- 开发框架
  - Vue3
- 组件库
  - ElementPlus
  - Echarts - 词云
  - Highcharts - 动态条形图
- 数据形式
  - 词云数据：元组，包含标签名和频数/权重
  - 动态条形图数据：JSON格式，按标签名组织，包含每个月的频数累计值