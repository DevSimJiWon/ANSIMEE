from flask import Flask, request
from google.cloud import vision
from google.cloud import automl
from firebase import Firebase
import detect

API_KEY = open('api_key.txt').readline().split(':')[1].strip()

project_id = "fluid-terminal-309613"
model_id = "IOD8333073282588737536"

config = {
  "apiKey": API_KEY,
  "authDomain": "theft-detection-675d4.firebaseapp.com",
  "databaseURL": "theft-detection-675d4-default-rtdb.firebaseio.com", #"https://pi-test-53efb-default-rtdb.firebaseio.com",
  "storageBucket": "theft-detection-675d4.appspot.com"
}

firebase = Firebase(config)
storage = firebase.storage()

app = Flask(__name__)

@app.route('/')
def hello_world():
    return "HELLO WORLD!"

@app.route('/detect_pack')
def detect_object():
    fname = request.args.get('file_name')
    storage.child("package_list/" + str(fname)).download('images/' + str(fname))
    res = detect.localize_objects(fname)    
    return res
    
if __name__ == '__main__':
    app.run(host='192.168.45.253', port=10025)
    #app.run(host='192.168.1.8', port=8080)
    
    
    

    
    
    
    
    
#================================================================================
# def localize_objects():
#     a = request.args.get('file_name')
#     storage.child("package_list/" + str(a)).download(str(a))
#     #path = 'https://firebasestorage.googleapis.com/v0/b/theft-detection-675d4.appspot.com/o/test%2F20181025_135614.jpeg?alt=media&token=b5f4d665-2e1d-4bf8-9fc3-e33bb646f5b0'
#     path = str(a)
#     client = vision.ImageAnnotatorClient()

#     with open(path, 'rb') as image_file:
#         content = image_file.read()
#     image = vision.Image(content=content)

#     objects = client.object_localization(
#         image=image).localized_object_annotations
    
#     d = {}
#     i = 0
#     for object_ in objects:
#         if object_.name in d.keys():
#             if object_.score > d[object_.name]:
#                 d[object_.name] = object_.score
#         else:
#             d[object_.name] = object_.score

#     s = {'result': 0}
#     x = ["Box", "Packaged goods", "Shipping box"]

#     flag = 0
#     for k, v in d.items():
#         if k in x:
#             flag = 1
#             s[k] = v
        
#     if flag == 1:
#         s['result'] = 1

#     return s 