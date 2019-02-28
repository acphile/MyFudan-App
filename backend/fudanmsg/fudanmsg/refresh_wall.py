# coding=utf-8
from .fudan import fudan
from django.http import HttpResponse
#请求表白墙爬虫，直接调用程序
def refresh_wall(request):
	if (request.POST['password'] == 'zzr0404001213'):
		usr = "16307130126"
		pwd = "zzr0404001213"
		d = fudan(usr,pwd)
		try:
			d.climb_wall()
		except Exception as e:
			return HttpResponse(e)
		finally:
			d.close()
		return HttpResponse("climb success")