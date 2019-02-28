# -*- coding: utf-8 -*-
from django.db import models

# Create your models here.
#用户账号和密码
class user(models.Model):
    name = models.CharField(max_length=20, unique=True)
    password = models.CharField(max_length = 256)
#表白墙的的标题、路径和时间信息
class wall_art(models.Model):
    title = models.CharField(max_length = 1024,unique=True)
    file_path = models.FileField()
    update_time = models.DateTimeField(auto_now_add = True)
	
#已废弃
class news(models.Model):
    title = models.CharField(max_length=1024,unique=True)
	
#新闻的路径和更新信息
class news_file(models.Model):
    file_path = models.FileField()
    update_time = models.DateTimeField(auto_now_add = True)
