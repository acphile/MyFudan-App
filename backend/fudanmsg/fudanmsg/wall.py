from model.models import user
from model.models import wall_art
from .fudan import fudan
from django.http import HttpResponse
import time
import datetime
from ast import literal_eval
#返回表白墙有关键字的内容
def wall(request):
	#key_word：关键字
	key_word = request.POST['key_word']
	last_time_str = request.POST['time']
	#获取最近一次访问以来的表白墙更新内容
	last_time = datetime.datetime.strptime(last_time_str,'%Y-%m-%d %H:%M:%S')
	#获取文件地址
	p = wall_art.objects.filter(update_time__gte = last_time)
	wall_lt = []
	for x in p:
		path_str = str(x.file_path)
		print(path_str)
		#读取文件抽出关键字部分
		file = open(path_str,"r",encoding='utf-8')
		content = file.read()
		lt = literal_eval(content)
		print(lt)
		for j in lt:
			if (key_word in j[0]):
				wall_lt.append(j)
		file.close()
	print(wall_lt)
	wall_lt=sorted(wall_lt,key=lambda x: datetime.datetime.strptime(x[1],"%Y年%m月%d日"))
	#第一部分为返回消息数量，消息之间以\n为分隔符，消息元组（包含时间戳和内容）以\t为分隔符
	result = str(len(wall_lt))+"\n"
	for x in wall_lt:
		result += x[0] + "\t" + x[1] + "\n"
	return HttpResponse(result)
