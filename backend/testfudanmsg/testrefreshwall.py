import requests
import time
while True:
	url = "http://119.23.240.17:8000/refresh-wall/"
	req = requests.post(url = url,data = {"password":"zzr0404001213"})
	print(req.text)
	time.sleep(86400)
