from django.http import HttpResponse
from selenium import webdriver
from  model.models import user
from .fudan import fudan
#用于测试，已废弃
#用于获取复旦大学uis账号和密码登录
def login(request):
	#name:学号 pwd：密码
    name = request.POST['name']
    pwd = request.POST['password']
	#通过调用复旦的类爬虫
    try:
        p = user.objects.get(name = name)
        if (p.password == pwd):
            return HttpResponse("login success")
        else:
            d = fudan(name,pwd)
            result = d.isTrue()
            d.close()
            if (result == True):
                p.password = pwd
                p.save()
                return HttpResponse("login success")
            else:
                return HttpResponse(result)
    except Exception:
        d = fudan(name,pwd)
        result = d.isTrue()
        d.close()
        if (result == True):
            p = user(name = name , password = pwd)
            p.save()
            return HttpResponse("login success")
        else:
            return HttpResponse(result)
