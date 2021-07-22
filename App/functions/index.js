const functions = require("firebase-functions");
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = admin.firestore();


exports.sendAlarm = functions.firestore.document("RaspberryPi/rob").onUpdate(async (event) => {
    const uid = event.after.get('uid') ;
    let userDoc = await admin.firestore().doc(`users/${uid}`).get();
    let boolDoc = await admin.firestore().doc(`RaspberryPi/rob`).get();
    let fcmToken = userDoc.get('fcm');
    //const cityRef = db.collection('RaspberryPi').doc('rob');

    const r_bool = boolDoc.get('r_bool');
    if(r_bool == 1 ){
        var message = {
            notification:{  
                title: '도난 발생 ! ',
                body:'도난이 감지 되었습니다. 도난 내역을 확인하세요.',
            },
            token:fcmToken,
        }   
        //r_bool값false로 바꾸기
        //const res = await cityRef.set({
          //  r_bool: false
          //}, { merge: true });

    }
let response = await admin.messaging().send(message);
console.log(response);

});