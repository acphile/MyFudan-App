import requests
url = "http://119.23.240.17:8000/classtable/"
req = requests.post(url = url,data = {"name":"16307130126","password":"zzr0404001213","year":"2018-2019","xq":"2学期"})
print(req.text)
