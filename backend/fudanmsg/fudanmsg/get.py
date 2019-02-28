import urllib
import socket
import hashlib
import urllib.request
from ast import literal_eval
from urllib.parse import quote
from bs4 import BeautifulSoup
import re 
import datetime as dt
import os
from django.http import HttpResponse
import datetime
from model.models import news
from model.models import news_file
import time

#爬去当前url的html文本
def getHtml(url):
    try:
        page=urllib.request.urlopen(url)
    except:
        print("打开失败：",url)
        return None
    content=page.read()
    return content
  
def hash_md5(content):
    res=hashlib.md5(content).hexdigest()
    return res
#获取标题
def getTitle(news):
    return news["title"] if news.get("title") is not None else news.string
#解析计算机学院网站新闻
def deal_cs(content):
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    parts=soup.find_all(name='div',attrs={"class":re.compile("^ui-item")})
    res=[]
    for part in parts:
        news_lis=part.find_all(name='p',attrs={"class":"tit"})
        news_lis.extend(part.find_all(name='li',attrs={"class":None}))
        res.extend([(news.a["href"],getTitle(news.a)) for news in news_lis])
    return res

#解析复旦大学外事处新闻
def deal_fao(content):
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    parts=soup.find_all(name='td',attrs={"align":"left"})
    res=[]
    for part in parts:
        try:
            if part.a['href'].startswith('/'):
                html="http://www.fao.fudan.edu.cn"+part.a['href']
        except:
            html=None
        if html:
            res.append((html,getTitle(part.a)))
    return res
#前端已废弃，但后端仍在爬取
#解析复旦大学校医院新闻
def deal_hos(content):
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    part=soup.find_all(name='div',attrs={"id":"wp_news_w12"})[0]
    news_lis=part.find_all(name='a',attrs={"target":"_blank","href":re.compile("(.)+(/(.)+)+")})
    return [("http://hospital.fudan.edu.cn"+news["href"],getTitle(news)) for news in news_lis]
    """
    for news in news_lis:
        print(news["href"],news["title"])
    print()
    """
#解析复旦大学体教部新闻
def deal_fdty(content):
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    news_lis=soup.find_all(name='a',attrs={"target":"_blank","href":re.compile("^news\.aspx\?id=")})
    return [("http://www.fdty.fudan.edu.cn/"+news["href"],getTitle(news)) for news in news_lis]
    """
    for news in news_lis:
        print(news["href"],news.string)
    print()        
    """
#解析复旦大学教务处新闻
def deal_jwc(content):
    print(dt.datetime.now())
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    news_lis=soup.find_all(name='a',attrs={"target":"_blank","href":re.compile("^/[a-z\d]")})
    return [("http://www.jwc.fudan.edu.cn"+news["href"],getTitle(news)) for news in news_lis]
    """
    for news in news_lis:
        title=getTitle(news)
        date_lis=news.parent.find_all(attrs={"class":re.compile("([dD]ate)|(time)")})
        date=date_lis[0].string if len(date_lis)>0 else None                
        if date is not None:
            date=dt.datetime.strptime(date,"%Y-%m-%d")
        up=date+dt.timedelta(days=100)>dt.datetime.now() if date is not None else True    
        if up:
            print(news["href"],title,date)
    print()  
    """
#解析复旦大学主网站新闻
def deal_fdu(content):
    #print(dt.datetime.now())
    if content is None:
        return
    soup=BeautifulSoup(content,"html.parser")
    news_lis=soup.find_all(name='a',attrs={"href":re.compile("^(\d)+(/(.)+)+")})
    return [("http://news.fudan.edu.cn/"+news["href"],getTitle(news)) for news in news_lis]
    """
    for news in news_lis:
        title=getTitle(news)
        date_lis=news.parent.find_all(attrs={"class":re.compile("[dD]ate")})
        date=date_lis[0].string if len(date_lis)>0 else None                
        if date is not None:
            date=dt.datetime.strptime(date,"%Y-%m-%d")
        up=date+dt.timedelta(days=2)>dt.datetime.now() if date is not None else True    
        if up:
            print(news["href"],title,date)
    print()  
    """

def add(res,file):
    lt=[]
    for x in res:
        if (x[0]!=None):
            try:
                p = news.objects.get(title = x[0])
            except Exception as e:
                #print(e)
                y = (x[0],x[1],getTime_url(x[0]))
                lt.append(y)
                p = news(title=x[0])
                p.save()

    file.write(str(lt))
    file.write("\n")
    return lt
#获取新闻时间，格式化时间（用于排序）
def getTime_url(url):
    content=getHtml(url)
    if content is None: return str(dt.datetime.now().date())
    soup=BeautifulSoup(content,"html.parser")
    tp=re.compile("[\d]{4}[-/\.][\d]{1,2}[-/\.][\d]{1,2}")
    times=soup.find_all(name='div',attrs={"class":"time"},text=tp)
    if len(times)==0:
        times=soup.find_all(name='span',text=tp)
        
    if len(times)==0:
        return str(dt.datetime.now().date())
    else:
        return tp.findall(times[0].string)[0].replace(".","-").replace("/","-")
	
#爬去新闻主函数
def refresh_news(request):
    if (request.POST['password'] != 'zzr0404001213'):
        return HttpResponse("fail")

    file_name = '/home/fudanmsg/news/' + str(int(time.time())) + '.txt'
    file = open(file_name,"w",encoding='utf-8')

    content_cs = getHtml("http://www.cs.fudan.edu.cn/")
    content_fao = getHtml("http://www.fao.fudan.edu.cn/")
    content_hos = getHtml("http://hospital.fudan.edu.cn/")
    content_fdty = getHtml("http://www.fdty.fudan.edu.cn/")
    content_fdu = getHtml("http://news.fudan.edu.cn/")
    content_jwc = getHtml("http://www.jwc.fudan.edu.cn/")
    res = deal_cs(content_cs)
    lt = add(res,file)

    res = deal_fao(content_fao)
    lt = add(res,file)

    res = deal_hos(content_hos)
    lt = add(res,file)

    res = deal_fdty(content_fdty)
    lt = add(res,file)

    res = deal_fdu(content_fdu)
    lt = add(res,file)

    res = deal_jwc(content_jwc)
    lt = add(res,file)

    file.close()
    p = news_file(file_path = file_name)
    p.save()
    return HttpResponse("climb success")
#前端请求新闻是返回更新的新闻
def get_news(request):
    last_time_str = request.POST["time"]
	#last_time：最近一次访问时间
    last_time = datetime.datetime.strptime(last_time_str, "%Y-%m-%d %H:%M:%S")
    print(last_time)
    #q = news_file.objects.all()
    #for i in q:
    #    print(i.update_time)
	#p:所有更新的新闻的内容
    p = news_file.objects.filter(update_time__gte=last_time)
    #for i in p:
    #    print(i.update_time)
    #    print(i.update_time>last_time)
    lt = [[]for i in range(6)]
    for x in p:
		#获取文本的存放地址并读取
        path_str = str(x.file_path)
        print(path_str)
        file = open(path_str, "r", encoding='utf-8')
        for i in range(6):
            content = file.readline()
            tmp_lt = literal_eval(content)
            lt[i].extend(tmp_lt)
        file.close()
    result=""
    for i in range(6):
        lt[i]=sorted(lt[i],key=lambda x:dt.datetime.strptime(x[2],"%Y-%m-%d"))
		#一共六部分，每部分以一个数字开始，表示该网站新闻数量，每段以\n为分隔符，段内元组以\t为分隔符
        result+=str(len(lt[i]))+"\n"
        for x in lt[i]:
            result+=str(x[0])+"\t"+str(x[1])+"\t"+str(x[2])+"\n"
#    print(result)
    return HttpResponse(result)


'''
if __name__=="__main__":
    #content_cs=getHtml("http://www.cs.fudan.edu.cn/")
    content_fdu = getHtml("http://news.fudan.edu.cn/")
    res=deal_fdu(content_fdu)
    print(res,len(res))
'''
    #header = urllib.request.urlopen("http://www.fdty.fudan.edu.cn/news.aspx?id=667").info()
    #print(header)
    
    #content_fao=getHtml("http://www.fao.fudan.edu.cn/")
    #print(hash_md5(content_fao))
    #res=deal_fao(content_fao)
    #print(len(res),res)
    #content_hos=getHtml("http://hospital.fudan.edu.cn/")
    #res=deal_hos(content_hos)
    #print(res,len(res))
    #content_fdty=getHtml("http://www.fdty.fudan.edu.cn/")
    #res=deal_fdty(content_fdty)
    #print(res,len(res))
    #content_fdu=getHtml("http://news.fudan.edu.cn/")
    #res=deal_fdu(content_fdu)
    #print(res,len(res))
    #content_jwc=getHtml("http://www.jwc.fudan.edu.cn/")
    #res=deal_jwc(content_jwc)
    #print(res,len(res))
    
    
