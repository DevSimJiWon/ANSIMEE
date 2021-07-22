import cv2 #opencv
import numpy as np 
import firebase_admin  #파이어 베이스 DB 연동관련 라이브러리
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import firestore
import pyrebase #파이어 베이스 스토리지 연동관련 라이브러리
from os import makedirs,listdir, truncate  #폴더만들기
from os.path import isdir, isfile, join
import shutil #파일옮기기
from threading import Thread

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

#유저 시리얼 넘버를 데이터베이스에서 가져오는 함수
def get_sn():
    DBLoginUser_SN = db.reference('username') #UserSN(현재는 username) 은 유저의 시리얼넘버
    global LoginUserSN
    LoginUserSN = DBLoginUser_SN.get() #유저의 시리얼 넘버를 가져온다

#유저의 닉네임을 가져온다
def get_name():
    global LoginUserName
    LoginUserName = rob_ref.get().get('username2') #유저의 이름를 가져온다

#유저 이미지 다운로드
def get_userdata():
    get_sn()
    get_name()

    if not isdir('./user_data/'+LoginUserSN):     # 해당 이름의 폴더가 없다면 생성
        makedirs('./user_data/'+LoginUserSN)

    dir_userdata = "./user_data/" + LoginUserSN +"/"
    DBdir_userdata = "user_image/" + LoginUserSN + "/" + LoginUserName #+ "/" + LoginUserName + ".jpg"  ## "Peter.jpg" 대신에
    storage.child(DBdir_userdata).download( "./" , LoginUserName ) # LoginUserName + ".jpg"
    shutil.move('./' + LoginUserName , dir_userdata + LoginUserName + ".jpg") # 다운로드 받은 파일을 user_data로 이동
    print("user data download complicate")

#도난감지 이미지 다운로드
def get_robdata():
    DBdir_robdata = "image_list/lastrob.jpg"
    storage.child(DBdir_robdata).download( "./" ,"lastrob.jpg")
    shutil.move( './' + 'lastrob.jpg' , './rob_data/lastrob.jpg') # 다운로드 받은 파일을 rob_data로 이동
    print("rob data download complicate")

# 얼굴 인식용 haar/cascade 로딩
face_dirs = 'faces/'
face_classifier = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')   

# face_detect
def face_detect(img):
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY) #흑백으로
    faces = face_classifier.detectMultiScale(gray,1.3,5) 
    
    if faces is():
        return None
    
    for(x,y,w,h) in faces:  #얼굴 크롭
        cropped_face = img[y:y+h, x:x+w] 
    return cropped_face


# DataPreprocessing
def DataPreprocessing(name):
    if not isdir(face_dirs+name):  # 해당 이름의 폴더가 없다면 생성
        makedirs(face_dirs+name)

    while True:
        frame = cv2.imread("./user_data/"+ LoginUserSN +"/" + LoginUserName + ".jpg") #LoginUserName + ".jpg"
        
        # 사진에서 얼굴 검출
        if face_detect(frame) is not None:
            face = cv2.resize(face_detect(frame),(200,200)) # 200 x 200 사이즈
            face = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY) # 흑백으로 바꿈

#           augflip = cv2.resize(face_detect(frame),(200,200)) # 200 x 200 사이즈
#           augflip = cv2.flip(face_detect(augflip),1) #좌우반전
#           augflip = cv2.cvtColor(augflip, cv2.COLOR_BGR2GRAY) # 흑백으로 바꿈            
  
            file_name_path = face_dirs + name + '/'+ LoginUserName +'.jpg' # faces/DBname/ 에 저장 
#           file_name_path_flip = face_dirs + name + '/user2'+'.jpg' # 데이터 증강 저장위치

            cv2.imwrite(file_name_path,face)
#           cv2.imwrite(file_name_path_flip,augflip) # 데이터 증강
            print("DataPreprocessing complicate")

#           cv2.putText(face,str(LoginUserSN),(50,50),cv2.FONT_HERSHEY_COMPLEX,1,(0,255,0),2)
#           cv2.imshow('Face Cropper',face)
            break

        else:
            print("DataPreprocessing fail")
            pass
        
#        if cv2.waitKey(1)==13 : 결과보기위에서 지움 
#            break

    cv2.destroyAllWindows()


####################################얼굴 인식##############################
store = firestore.client()

#업데이트
rob_ref = store.collection(u'RaspberryPi').document(u'rob')

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
        rob_frame = cv2.imread("./rob_data/lastrob.jpg")
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
                break
            else:
            #75 이하면 타인
#               cv2.putText(image, "Locked", (250, 450), cv2.FONT_HERSHEY_COMPLEX, 1, (0, 0, 255), 2)
#               cv2.imshow('Face Cropper', image)
                print("locked : "+ min_score_name +" "+ display_string)
                #파이어베이스 업데이트################################# 도난발생
                rob_ref.update({u"r_bool" : False})
                rob_ref.update({u"uid" : LoginUserSN})
                break
        except:
            #얼굴 검출 안됨 
#           cv2.putText(image, "Face Not Found", (250, 450), cv2.FONT_HERSHEY_COMPLEX, 1, (255, 0, 0), 2)
#           cv2.imshow('Face Cropper', image)
            #파이어베이스 업데이트################################# 도난발생
            print("Face not Found")
            rob_ref.update({"r_bool" : True})
            rob_ref.update({u"uid" : LoginUserSN})
            break
#       if cv2.waitKey(1)==13:
#           break
#   rob_frame.release()
    cv2.destroyAllWindows()

##메인
while(True):
    while(True):
        try:
            dir_Userupdate_bool = db.reference('Userupdatebool') #로그인이 되어있는지 확인하기 위한 부울값
            Userupdate_bool = dir_Userupdate_bool.get()
            if Userupdate_bool == "True": #접속이 확인된다면
                get_userdata() #유저 데이터를 다운받고
                DataPreprocessing(LoginUserSN) #데이터 전처리
                print("Userupdate Success")
                break #탈출
            else:
                print("Userupdate fail")
        except:
            print("Userupdate error")