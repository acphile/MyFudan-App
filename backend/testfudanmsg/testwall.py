#coding=utf-8

import requests
url = "http://119.23.240.17:8000/wall/"
req = requests.post(url = url,data = {"key_word":"","time":"2010-1-1 10:00:00"})
print(req.text)
