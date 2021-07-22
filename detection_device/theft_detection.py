# 파일명: theft_detection.py
# 파일설명: 택배 도난 감지 기기를 실행시키고 도난을 감지하는 모듈을 구현함.
# 개발자: 신예나
# 개발일: 2021.05.

import RPi.GPIO as GPIO
import time
import datetime
import picamera
import sys, os
import requests
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import firestore
from uuid import uuid4

url = "http://218.239.22.19:10025/detect_pack" # 택배 인식 알고리즘 서버 url

PROJECT_ID = "theft-detection-675d4" # firebase project ID
cred = credentials.Certificate("../Downloads/theft-detection-675d4-firebase-adminsdk-3tpl1-1f2a8cdb45.json")
default_app = firebase_admin.initialize_app(cred, {
    'storageBucket': f"{PROJECT_ID}.appspot.com"
});
bucket = storage.bucket()
db = firestore.client()

GPIO.setmode(GPIO.BCM)
GPIO.setup(21,GPIO.OUT) # buzzer 센서
GPIO.setup(24,GPIO.IN)  # pir 센서

scale = [1047, 880, 1047, 880, 1047, 880, 1047, 880]

# ----------------------- firebase에 파일을 업로드 하는 함수 ------------------------------------
def fileUpload(file, folder):
    blob = bucket.blob(folder + file)
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token}
    blob.metadata = metadata
    blob.upload_from_filename(filename=file, content_type='image/jpeg')
    print("파일 업로드 완료 [ 폴더명: " + folder + " 파일명: " + file + " ]")

# ----------------------- 도난 여부를 받아와 부저를 제어하는 함수 ------------------------------------
def execute_buzzer():
    doc_ref = db.collection(u'RaspberryPi').document(u'rob')
    doc = doc_ref.get()
    while doc.get('checked') == False: # 사용자 판단 알고리즘에서 판단이 완료되면 checked를 true로 바꿈
        print("값을 기다리는 중...")
        time.sleep(1)
        doc_ref = db.collection(u'RaspberryPi').document(u'rob')
        doc = doc_ref.get()
        if doc.get('checked') == True:
            break

    doc_ref = db.collection(u'RaspberryPi').document(u'rob')
    doc = doc_ref.get()
    if doc.get('r_bool') == True: # 도난인 경우
        print("도난 발생")
        p = GPIO.PWM(21, 600)
        p.start(50.0)
        for i in range(8):
            p.ChangeFrequency(scale[i])
            time.sleep(0.5)
        p.stop()
        doc_ref.update({
            u'r_bool' : False # 도난 여부를 false로 변경
        })
    else: # 도난이 아닌 경우
        print("택배를 정상수령하였습니다.")
    doc_ref.update({
        'checked' : False # 사용자 판단 여부를 false로 변경
    })
    doc_ref2 = db.collection(u'RaspberryPi').document(u'parcel')
    doc_ref2.update({
        u'p_bool' : False # 택배 존재 여부를 false로 변경
    })

# ----------------------- 택배 인식 여부를 받아와 택배 존재 여부 값을 변경하는 함수 ------------------------------------
def parcel_recognition(filename):
    doc_ref = db.collection(u'RaspberryPi').document(u'parcel')
    doc = doc_ref.get()
    fileUpload(filename, 'package_list/') # 촬영한 사진을 package_list에 업로드 (택배 인식 알고리즘에서 사용)
    d = {'file_name' : filename}
    res = requests.get(url, params=d) # 택배 인식 알고리즘에 택배 인식 값 요청
    q = res.json()
    if q['result'] is 1: # 택배를 인식한 경우
        print("택배 인식")
        if doc.get('p_bool') == False:
             doc_ref.update({
                 u'p_bool' : True # 택배 존재 여부를 true로 변경
             })
             print("신규 택배가 등록되었습니다.")
        else:
            print("택배가 보관중입니다.")
    else: # 택배를 인식하지 못한 경우
        print("택배를 인식하지 못했습니다.")
        if doc.get('p_bool') == True:
            print("택배가 사라짐")
            fileUpload(filename, 'image_list/') # 촬영한 사진을 image_list에 업로드 (사용자 판단 알고리즘에서 사용)
            doc_ref2 = db.collection(u'RaspberryPi').document(u'face')
            doc_ref2.update({
                u'checked' : True # 사용자 판단 알고리즘에 사용자 판단을 요청하는 값
            })
            execute_buzzer() # 부저를 제어하는 함수로 이동

# ----------------------- 택배 도난 감지 기기의 카메라를 제어하는 함수 ------------------------------------
def execute_camera():
    camera = picamera.PiCamera()
    camera.resolution = (2592,1944)
    camera.framerate = 15
    camera.start_preview()
    time.sleep(1)
    now = datetime.datetime.now()
    filename = 'theft.jpg'
    camera.capture(filename)
    camera.stop_preview()
    parcel_recognition(filename) # 촬영한 사진과 함께 택배 인식 함수로 이동
    camera.close()


try:
    while True:
        if GPIO.input(24) == True: # 사람이 인식된 경우
            print("PIR SENSOR ON")
            execute_camera() # 카메라를 제어하는 함수로 이동
        if GPIO.input(24) == False: # 사람이 인식되지 않은 경우
            print("PIR SENSOR OFF")
        time.sleep(0.5)

except KeyboardInterrupt:
    GPIO.cleanup()
finally:
    GPIO.cleanup()
