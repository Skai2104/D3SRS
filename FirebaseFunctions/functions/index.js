'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendSOSNotification = functions.firestore
									.document("Users/{user_id}/SOSNotification/{notification_id}")
									.onWrite((event, context) => {
										const user_id = context.params.user_id;
										const notification_id = context.params.notification_id;
										let from_user;
										let from_user_id;
										let from_message;
										let latitude;
										let longitude;
										let datetime;

										return admin.firestore().collection("Users").doc(user_id)
																.collection("SOSNotification").doc(notification_id)
																.get().then(queryResult => {
																	from_user = queryResult.data().from;
																	from_user_id = queryResult.data().fromId;
																	from_message = queryResult.data().message;
																	latitude = queryResult.data().latitude;
																	longitude = queryResult.data().longitude;
																	datetime = queryResult.data().datetime;

																	const from_data = admin.firestore().collection("Users").doc(from_user_id).get();
																	const to_data = admin.firestore().collection("Users").doc(user_id).get();

																	return Promise.all([from_data, to_data]);
																})
																.then(result => {
																	const from_name = result[0].data().name;
																	const to_name = result[1].data().name;
																	const token_id = result[1].data().token_id;

																	const payload = {
																		notification: {
																			title: "SOS from " + from_name + "!",
																			body: from_message,
																			icon: "default",
																			click_action: "com.skai2104.d3srs.TARGETSOSNOTIFICATION"
																		},
																		data: {
																			message: from_message,
																			from_user: from_user,
																			latitude: latitude,
																			longitude: longitude,
																			datetime: datetime
																		} 
																	};

																	return admin.messaging().sendToDevice(token_id, payload);
																})
																.then(result => {
																	return console.log("SOS Notification sent.");
																});
									});

exports.sendStatusUpdate = functions.firestore
									.document("Users/{user_id}/StatusNotification/{notification_id}")
									.onWrite((event, context) => {
										const user_id = context.params.user_id;
										const notification_id = context.params.notification_id;
										let from_user;
										let from_user_id;
										let from_message;
										let latitude;
										let longitude;
										let datetime;
										let status;

										return admin.firestore().collection("Users").doc(user_id)
																.collection("StatusNotification").doc(notification_id)
																.get().then(queryResult => {
																	from_user = queryResult.data().from;
																	from_user_id = queryResult.data().fromId;
																	from_message = queryResult.data().message;
																	latitude = queryResult.data().latitude;
																	longitude = queryResult.data().longitude;
																	datetime = queryResult.data().datetime;
																	status = queryResult.data().status;

																	const from_data = admin.firestore().collection("Users").doc(from_user_id).get();
																	const to_data = admin.firestore().collection("Users").doc(user_id).get();

																	return Promise.all([from_data, to_data]);
																})
																.then(result => {
																	const from_name = result[0].data().name;
																	const to_name = result[1].data().name;
																	const token_id = result[1].data().token_id;

																	const payload = {
																		notification: {
																			title: "Safety status update from " + from_name + ": " + status,
																			body: from_message,
																			icon: "default",
																			click_action: "com.skai2104.d3srs.TARGETSTATUSNOTIFICATION"
																		},
																		data: {
																			message: from_message,
																			from_user: from_user,
																			latitude: latitude,
																			longitude: longitude,
																			datetime: datetime,
																			status: status
																		}
																	};
																	return admin.messaging().sendToDevice(token_id, payload);
																})
																.then(result => {
																	return console.log("Status Notification sent.");
																});

									});						