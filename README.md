# hy.common.berkeley



嵌入式(文件)数据库Berkeley

 * 1. 同时创建两个数据库。
 * 2. 一个是存储真实数据的数据库。即普通的数据库key/value。
 * 3. 一个存储可序列化对象的结构的数据库。
 * 4. 当存储一个可序列化对象时，先在真实数据的数据库中存储key及对象的序列化值。
 * 5. 当存储一个可序列化对象时，后在可序列化的数据库中存储同样的key及Java对象的元类的全名称。



=====
#### 本项目引用Jar包，其源码链接如下
引用 https://github.com/HY-ZhengWei/hy.common.base 类库

引用 https://github.com/HY-ZhengWei/hy.common.file 类库