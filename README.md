# CloudComputing

## 应用说明

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

## spark项目

- spark 在项目中打包生成jar文件
- 将jar文件上传至服务器
- 在服务器中以spark submit方式运行jar包
- 监听hdfs文件夹/input_data
- 每当有文件上传至hdfs中则会对其进行处理生成txt文件保存并供前端使用