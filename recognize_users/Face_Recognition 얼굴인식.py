import cv2 #opencv
import numpy as np 
import firebase_admin  #파이어 베이스 DB 연동관련 라이브러리
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import firestore
import pyrebase #파이어 베이스 스토리지 연동관련 라이브러리
from os import makedirs,listdir  #폴더만들기
from os.path import isdir, isfile, join
import shutil #파일옮기기
from threading import Thread
import datetime as dt #시간

#파이어베이스 데이터베이스 인증
cred = credentials.Certificate('theft-detection-675d4-firebase-adminsdk-3tpl1-2a9e46282d.json') #인증키 1V
firebase_admin.initialize_app(cred,{
    "projectId": "theft-detection-675d4",
    'databaseURL' : 'https://theft-detection-675d4-default-rtdb.firebaseio.com/' #1V
    })

config = { #파이어 베이스 스토리지 연동관련 1V
    "apiKey": "AIzaSyAzsMYVmUcv_qIsoTR8GTRDoN6Tq2gXab8",
    "authDomain": "theft-detection-675d4.firebaseapp.com",
    "databaseURL": "https://theft-detection-675d4-default-rtdb.firebaseio.com",
    "projectId": "theft-detection-675d4",
    "storageBucket": "theft-detection-675d4.appspot.com",
    "messagingSenderId": "103969772175",
    "appId": "1:103969772175:web:c843c3c105fbc84d94d6fa",
    "measurementId": "G-V1KQHD5ZK4"
}

#파이어 베이스 스토리지 다운로드
firebase = pyrebase.initialize_app(config)
storage = firebase.storage()
####################################얼굴 인식##############################
store = firestore.client()

def get_sn():
    DBLoginUser_SN = db.reference('username') #UserSN(현재는 username) 은 유저의 시리얼넘버
    global LoginUserSN
    LoginUserSN = DBLoginUser_SN.get() #유저의 시리얼 넘버를 가져온다

# 얼굴 인식용 haar/cascade 로딩
face_dirs = 'faces/'
face_classifier = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')   


#도난감지 이미지 다운로드
def get_robdata():
    DBdir_robdata = "image_list/theft.jpg"
    storage.child(DBdir_robdata).download( "./" ,"theft.jpg")
    shutil.copy( './' + 'theft.jpg' , './rob_data/theft.jpg') # 다운로드 받은 파일을 rob_data로 이동
    print("rob data download complicate")

#업데이트
rob_ref = store.collection(u'RaspberryPi').document(u'rob')
checked_ref = store.collection(u'RaspberryPi').document(u'face') # 실행용

# 사용자 얼굴 학습
def train(name):
    data_path = 'faces/' + name + '/'
    #파일만 리스트로 만듬
    face_pics = [f for f in listdir(data_path) if isfile(join(data_path,f))]
    
    Training_Data, Labels = [], []
    
    for i, files in enumerate(face_pics):
        image_path = data_path + face_pics[i]
        images = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        # 이미지가 아니면 패스
        if images is None:
            continue    
        Training_Data.append(np.asarray(images, dtype=np.uint8))
        Labels.append(i)
    if len(Labels) == 0:
        print("There is no data to train.")
        return None
    Labels = np.asarray(Labels, dtype=np.int32)
    # 모델 생성
    model = cv2.face.LBPHFaceRecognizer_create()
    # 학습
    model.train(np.asarray(Training_Data), np.asarray(Labels))
    print(name + " : Model Training Complete")

    #학습 모델 리턴
    return model

# 여러 사용자 학습
def trains():
    #faces 폴더의 하위 폴더를 학습
    data_path = 'faces/'
    # 폴더만 색출
    model_dirs = [f for f in listdir(data_path) if isdir(join(data_path,f))]
    
    #학습 모델 저장할 딕셔너리
    models = {}
    # 각 폴더에 있는 얼굴들 학습
    for model in model_dirs:
        print('model :' + model)
        # 학습 시작
        result = train(model)
        # 학습이 안되었다면 패스!
        if result is None:
            continue
        # 학습되었으면 저장
        print('model2 :' + model)
        models[model] = result

    # 학습된 모델 딕셔너리 리턴
    return models    

#얼굴 검출
def face_detector(img, size = 0.5):
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = face_classifier.detectMultiScale(gray,1.3,5)
        if faces is():
            return img,[]
        for(x,y,w,h) in faces:
            cv2.rectangle(img, (x,y),(x+w,y+h),(0,255,255),2)
            roi = img[y:y+h, x:x+w]
            roi = cv2.resize(roi, (200,200))
        return img,roi   #검출된 좌표에 사각 박스 그리고(img), 검출된 부위를 잘라(roi) 전달



# 인식 시작
def run(models):
    while True:
        rob_frame = cv2.imread("./rob_data/theft.jpg")
        # 얼굴 검출 시도 
        image, face = face_detector(rob_frame)
        count = 0

        try:            
            min_score = 999       #가장 낮은 점수로 예측된 사람의 점수
            min_score_name = ""   #가장 높은 점수로 예측된 사람의 이름
            
            #검출된 사진을 흑백으로 변환 
            face = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)

            #위에서 학습한 모델로 예측시도
            for key, model in models.items():
                result = model.predict(face)                
                if min_score > result[1]:
                    min_score = result[1]
                    min_score_name = key
                        
            if min_score < 500:
                confidence = int(100*(1-(min_score)/300))
                # 유사도 화면에 표시 
                display_string = str(confidence)+'% Confidence'
#           cv2.putText(image,display_string,(100,120), cv2.FONT_HERSHEY_COMPLEX,1,(250,120,255),2)
            #75 보다 크면 동일 인물
            if confidence > 75:
#               cv2.putText(image, "Unlocked : " + min_score_name, (10, 450), cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
#               cv2.imshow('Face Cropper', image)
                print("Unlocked : "+ min_score_name +" "+ display_string)
                #파이어베이스 업데이트#################################
                rob_ref.update({u"r_bool" : False})
                rob_ref.update({"checked" : True})
                break
            else:
            #75 이하면 타인
#               cv2.putText(image, "Locked", (250, 450), cv2.FONT_HERSHEY_COMPLEX, 1, (0, 0, 255), 2)
#               cv2.imshow('Face Cropper', image)
                print("locked : "+ min_score_name +" "+ display_string)
                #파이어베이스 업데이트################################# 도난발생
                rob_ref.update({u"r_bool" : True})
                rob_ref.update({u"uid" : LoginUserSN})
                rob_ref.update({"checked" : True})
                storage.child(DBdir_roblist).put("theft.jpg")
                break
        except:
            #얼굴 검출 안됨 
#           cv2.putText(image, "Face Not Found", (250, 450), cv2.FONT_HERSHEY_COMPLEX, 1, (255, 0, 0), 2)
#           cv2.imshow('Face Cropper', image)
            #파이어베이스 업데이트################################# 도난발생
            print("Face not Found")
            rob_ref.update({"r_bool" : True})
            rob_ref.update({u"uid" : LoginUserSN})
            rob_ref.update({"checked" : True})
            storage.child(DBdir_roblist).put("theft.jpg")
            break
#       if cv2.waitKey(1)==13:
#           break
#   rob_frame.release()
    cv2.destroyAllWindows()

##메인
while(True):
    while(True):
        get_sn()
        x = dt.datetime.now() #현재시간
        DBdir_roblist = "rob_list/"+ LoginUserSN +"/"+ x.strftime("%H시 %M분 %S초") + ".jpg"
        checked_bool = checked_ref.get().get('checked') #도난기기가 작동 되었는지 확인하기 위한 부울값
        if checked_bool == True: #도난기기의 작동이 확인된다면
            print("Connection Check Success")
            get_robdata() #도난 데이터를 다운받고
    #       frame = cv2.imread("./rob_data/lastrob.jpg")
    #       face = cv2.resize(face_detect(frame),(200,200)) # 200 x 200 사이즈
    #       face = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY) # 흑백으로 바꿈
    #       file_name_path = './rob_data/lastrob.jpg' # ./rob_data/lastrob.jpg 에 저장
    #       cv2.imwrite(file_name_path,face)
            models = trains()   # 학습 시작
            run(models)
            checked_ref.update({"checked" : False})
            break #탈출
        else:
            print("Connection Check fail")