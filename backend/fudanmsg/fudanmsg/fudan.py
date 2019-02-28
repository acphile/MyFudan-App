# coding=utf-8
'''
该文件只有一个类为fudan，所有利用selenium爬去文件的程序包含在该类中，还用到BeautifulSoup用来解析html文件，用lxml解析xpath
'''
from selenium import webdriver
from bs4 import BeautifulSoup
from model.models import wall_art
import time
import requests
from lxml import etree

class fudan:
	#初始化设置
    def __init__(self, name, pwd):
        
		self.username = name
        self.password = pwd
		#下面分别是复旦教务服务、复旦uis登录、微信搜狗的网址
        self.jwfw_url = "http://jwfw.fudan.edu.cn/eams/home.action"
        self.uis_url = "http://uis.fudan.edu.cn/authserver/login"
        self.sogou_url = "https://weixin.sogou.com/"
        options = webdriver.FirefoxOptions()
        options.set_headless()
        self.driver = webdriver.Firefox(firefox_options=options,log_path="/home/fudanmsg/geckodriver.log")
		
	#判断账号密码是否正确
    def isTrue(self):
        self.driver.get(self.uis_url)
        try:
            usr_em = self.driver.find_element_by_id("username")
            pwd_em = self.driver.find_element_by_id("password")
            usr_em.clear()
            pwd_em.clear()
            usr_em.send_keys(self.username)
            pwd_em.send_keys(self.password)
            self.driver.find_element_by_id("idcheckloginbtn").click()
        except Exception as e:
            return e
        try:
            self.driver.find_element_by_xpath("/html/body/div/div/div[3]/input[1]").click()
        except Exception:
            pass

        if ("uis.fudan.edu.cn/authserver/login" in self.driver.current_url):
            return "username or password is wrong"
        else:
            return True

	#进入复旦大学教务服务系统
    def start_jwfw(self):
        self.driver.get(self.jwfw_url)
        time.sleep(0.5)
        try:
            usr_em = self.driver.find_element_by_id("username")
            pwd_em = self.driver.find_element_by_id("password")
            usr_em.clear()
            pwd_em.clear()
            usr_em.send_keys(self.username)
            pwd_em.send_keys(self.password)
            self.driver.find_element_by_id("idcheckloginbtn").click()
            # test = self.driver.find_element_by_xpath("/html/body/div/div/div[3]/input[100]")
        except Exception:
            pass
        try:
            self.driver.find_element_by_xpath("/html/body/div/div/div[3]/input[1]").click()
        except Exception:
            pass

        try:
            self.driver.find_element_by_xpath("/html/body/a").click()
        except Exception:
            pass
            time.sleep(0.5)
        except Exception:
            pass

	#获取课表的html文件（<tbody>部分）
    def get_table(self, year, xq):
        self.driver.find_element_by_xpath('/html/body/table/tbody/tr/td[1]/div/ul/li[1]/ul/div/li[18]/a').click()
        time.sleep(0.5)

        self.driver.find_element_by_xpath(
            "/html/body/table/tbody/tr/td/div[@id='main']/div[2]/form/div[2]/input[1]").click()
        year_lt = self.driver.find_elements_by_xpath(
            '//html/body/table/tbody/tr/td/div/div[2]/form/div[2]/div/table[1]/tbody/tr/td')
        for y in year_lt:
            #print(y.text)
            if (y.text == year):
                y.click()
                break
        time.sleep(1)
        xq_lt = self.driver.find_elements_by_xpath(
            '//html/body/table/tbody/tr/td/div/div[2]/form/div[2]/div/table[2]/tbody/tr/td')
        for x in xq_lt:
            #print(x.text)
            if (x.text == xq):
                x.click()
                break
        time.sleep(1)
        self.driver.find_element_by_xpath("/html/body/table/tbody/tr/td[3]/div/div[2]/form/div[2]/input[2]").click()
        time.sleep(3)
        class_table_xml = self.driver.find_element_by_xpath(
            "/html/body/table/tbody/tr/td[3]/div/table/tbody/tr/td/div/table")
        return class_table_xml.get_attribute('innerHTML')

	#解析课表为矩阵
    def translate_table(self, year, xq):
        class_table_xml = self.get_table(year, xq)
        # print(class_table_xml)
        soup = BeautifulSoup(class_table_xml, features="html.parser")
        table = [[None for i in range(14)] for i in range(7)]
        soup = BeautifulSoup(class_table_xml, features="html.parser")
        class_y = soup.find('tbody').find_all('tr')
        for i in range(14):
            class_x = class_y[i].find_all('td')
            for j in range(len(class_x) - 1):
                x = 0
                while (table[x][i] != None):
                    x = x + 1
                if (class_x[j + 1].get('rowspan') != None):
                    v = int(class_x[j + 1]["rowspan"])
                    for k in range(v):
                        table[x][i + k] = class_x[j + 1]["title"]
                else:
                    table[x][i] = "None"
        return table

    def art_solve(self,s):
        lt = []
        content = self.driver.find_elements_by_xpath('/html/body/div/div/div/div/div/div/section/section/strong/span')
        for i in content:
            lt.append((i.text,s))
        return lt
	#爬去表白墙
    def climb_wall(self):
        self.driver.get(self.sogou_url)
        key = self.driver.find_element_by_xpath('// *[ @ id = "query"]')
        key.clear()
		#进入复旦微生活公众号
        key.send_keys("复旦微生活")
        self.driver.find_element_by_xpath('//*[@id="searchForm"]/div/input[4]').click()
		#获取所有文章的链接
        url = self.driver.find_element_by_xpath('/html/body/div/div/div/ul/li[1]/div/div/p/a').get_attribute('href')
        self.driver.set_page_load_timeout(10)
        self.driver.get(url)
        article_lt = self.driver.find_elements_by_xpath('/html/body/div/div/div/div/div/div/div/h4')
        time_lt = self.driver.find_elements_by_xpath('/html/body/div/div/div/div/div/div/div/p[2]')
		#判断之前是否爬去过
        left_lt = []
        for i in range(len(article_lt)):
            print(article_lt[i].get_attribute("innerHTML"))
            print(time_lt[i].get_attribute("innerHTML"))
            try:
                p = wall_art.objects.get(title = article_lt[i].get_attribute("innerHTML"))
                continue
            except Exception:
                print(article_lt[i].get_attribute("innerHTML"))
                if "复旦表白墙" in article_lt[i].get_attribute("innerHTML"):
                    left_lt.append((article_lt[i].get_attribute("innerHTML"),article_lt[i].get_attribute("hrefs"),time_lt[i].get_attribute("innerHTML")))
        print("=================")
        print(left_lt)
		#对于没有爬去的链接进行爬虫
        for art in left_lt:
            try:
                p = wall_art.objects.get(title = art[0])
            except Exception as e:
				#跳转该文章的链接
                self.driver.get("https://mp.weixin.qq.com"+art[1])         
                time.sleep(2)
				#生成相应的文件储存该文章的核心内容
                file_name = '/home/fudanmsg/wall_art/' + str(int(time.time())) + '.txt'
                file = open(file_name , "w" , encoding='utf-8')
                x = self.art_solve(art[2])
                file.write(str(x))
                file.close()
				#将该文件存入数据库
                p = wall_art(title=art[0], file_path=file_name)
                p.save()

    def close(self):
        self.driver.quit()
