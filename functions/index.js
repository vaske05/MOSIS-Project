// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

function distance(lat1, lon1, lat2, lon2, unit) {
	if ((lat1 === lat2) && (lon1 === lon2)) {
		return 0;
	}
	else {
		var radlat1 = Math.PI * lat1/180;
		var radlat2 = Math.PI * lat2/180;
		var theta = lon1-lon2;
		var radtheta = Math.PI * theta/180;
		var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
		if (dist > 1) {
			dist = 1;
		}
		dist = Math.acos(dist);
		dist = dist * 180/Math.PI;
		dist = dist * 60 * 1.1515;
		if (unit==="K") { dist = dist * 1.609344 }
		if (unit==="N") { dist = dist * 0.8684 }
		return dist;
	}
}

exports.radiusNotification = functions.database.ref('Users/{userId}').onUpdate((snapshot, context) => {

	const myLocation = snapshot.after.val().userLocation;
	const friendList = snapshot.after.val().friendsList || [];

	const payload = {
		notification: {
			title: 'Maybe your friend wants a coffee',
			body: `Seems like ${snapshot.after.val().name} is near`
		}
	};

	return admin.database().ref('Users').once('value').then(snapshot => {
		
		const friendUsers = Object.keys(snapshot.val()).filter(key => friendList.indexOf(key) !== -1 && key !== 'init');
		
		friendUsers.forEach(key => {
			const userLocation = snapshot.val()[key].userLocation;			
			const diffDistance = distance(userLocation.latitude, userLocation.longitude, myLocation.latitude, myLocation.longitude, 'K');

			if (diffDistance < 0.5) {
				return admin.messaging().sendToTopic(`radiusNotification-${snapshot.val()[key].id}`, payload);
			} else {
				return null;
			}
		})
	}).catch(e => console.error(e));
});