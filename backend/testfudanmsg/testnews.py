#coding=utf-8
import requests

url = "http://119.23.240.17:8000/news/"
req = requests.post(url = url,data = {"time":"2018-12-8 22:10:00"})
print(req.text)

