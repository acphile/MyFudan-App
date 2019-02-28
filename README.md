# MyFudan App

### 功能描述：

为了方便复旦学生获取信息，本次项目我们实现了一款能够自动获取复旦新闻、表白墙更新并能自动获取学生课表的App，命名为MyFudan。我们这款App主要具有以下功能：

1. 获取复旦主要网站的新闻更新
2. 获取包含指定关键字表白墙更新
3. 输入学号密码能够自动从jwfw获取学生的课表
4. 上课时间开启自定义锁屏（默认开启，可设置关闭）
5. 通知栏弹出更新提醒


### 部署与运行：


​目前本项目已发布为apk（见目录下MyFudan.apk），后端公网IP地址为119.23.240.17，apk安装后即可直接运行，如果需要重新部署服务端运行此项目，需要执行以下步骤：

1. 部署后端程序（backend文件夹），原运行环境为Ubuntu 16.04，Python 3.7.1：

   a. 安装依赖库selenium：pip3 install selenium；安装geckodriver：已经在/fudanmsg里，将其复制到/usr/bin里面，如有问题可以自己手动下载替换。

   b. 文件夹放置：由于历史遗留问题部分文件操作使用文件路径为绝对路径，因此建议将文件夹放在/home下，之前新闻爬取数据在/fudanmsg/news下，表白墙数据在/fudanmsg/wall_art下，刷新脚本在/testfudanmsg下，放置位置随意。

   c. 启动nohup python3 manage.py runserver 0.0.0.0:8000 &

   d. 更改刷新脚本testrefreshwall.py和testrefreshnews.py的IP地址，并保持后台常驻定期更新。

2. 修改前端代码（Newapp文件夹）中IP地址，部署的计算机或服务器相应的公网IP地址。前端访问服务端分别在以下位置： 

   login.java line41：获取课表所访问url

   wallFragment.javaline 62: 获取表白墙消息所访问url

   newsFragment.javaline 50: 获取新闻信息所访问的url

   notificationService.javaline 40, line 151

3. 重新build发布为apk

