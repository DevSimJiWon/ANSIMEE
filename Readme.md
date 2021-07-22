# 머신러닝 기반 IOT 택배 도난 감지 시스템

## 서비스 개요 

 해당 프로젝트는 딥러닝 기술과 IOT 기술을 이용하여 택배 도난 감지 시스템을 개발하는 것이다. 택배 도난 감지 기기에서 도난 여부를 판단할 수 있도록 개발 하였으며, 사용자는 택배 모니터링 앱을 통해 서비스를 이용하므로 사용에 편리하도록 고안되었다.

 사용자는 모바일 앱을 통해 사용자 데이터인 사용자 정보와 이미지를 등록한다. 서버로 Google Cloud Platform을 사용하며 모바일 앱은 서버에 사용자 데이터를 전하고 서버는 데이터베이스인 Firebase에 데이터를 저장한다. OpenCV에서는 얼굴 인식 알고리즘의 정확도를 높이기 위해 주기적으로 학습을 진행한다. Raspberry Pi는 도난 상황을 감지하고 도난 데이터를 서버에 전송하며, 이때 전송받은 데이터를 OpenCV로 전송하여 얼굴 인식 알고리즘을 진행한다. 인식 결과 서버에 결과값을 반환하고 도난으로 판단된 경우 사용자에게는 모바일을 통해 도난 알림을 보내고, Raspberry Pi로도 도난 알림을 보내 도난 감지 기기에서 도난 알람을 발생시키도록 한다.

## 기술 스택

​	프로젝트에서 사용한 기술 스택입니다.

### 	언어

​		python

​		Java

​		JavaScript

​	

### 	퍼블릭 클라우드

​		Firebase

​		Google Cloud Platform



## 환경 구성

### 		개발 및 운영환경

#### 				택배도난 감지 기기

| 개발 환경 | 규격                     |
| --------- | ------------------------ |
| 편집툴    | Atom, Visual Studio Code |
| 사용언어  | Python                   |
| OS        | Raspbian 5.4.72          |
| DB        | Firebase                 |
| H/W       | Raspvverry Pi 3 ModelB+  |

#### 				택배 도난 모니터링 앱

| 개발환경 | 규격                    |
| -------- | ----------------------- |
| 편집툴   | Android Studio, Node.js |
| 사용언어 | Java, JavaScript        |
| OS       | Android 11              |
| DB       | Firebase                |



### 		소스디렉터리 구조

#### 				택배도난 감지 기기

![dr1](https://user-images.githubusercontent.com/87285592/126587241-794d4faa-af96-4e5f-bbd3-6e5f2699816a.jpg)

#### 				택배 도난 모니터링 앱

![dr2](https://user-images.githubusercontent.com/87285592/126587247-279adf23-4656-4b4c-a38d-2ec64c5bf9be.jpg)

