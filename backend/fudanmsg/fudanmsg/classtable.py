#coding=utf-8
from .fudan import fudan
from django.http import HttpResponse

def classtable(request):
	#usr:学号 pwd：密码 year:学年 xq：学期
    usr = request.POST['name']
    pwd = request.POST['password']
    year = request.POST['year']
    xq = request.POST['xq']
	#调用fudan.py里面fudan类进行爬虫
    d = fudan(usr,pwd)
    result = d.isTrue()
    if (result == True):
        d.start_jwfw()
        class_table = None
        try:
            class_table = d.translate_table(year,xq)
        except Exception:
            try:
                d.start_jwfw()
                class_table = d.translate_table(year,xq)
            except Exception:
                #print("Error")
                return HttpResponse("driver Error")
        finally:
            d.close()
		#第一行为运行结果，后为内容，以\n\t为分隔符画出一张表格
        return HttpResponse("login success\n"+("\n".join(["\t".join(it) for it in class_table])) )
    else:
        d.close()
        return HttpResponse(result)