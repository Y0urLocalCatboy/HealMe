const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.notifyNewVisit = onDocumentWritten("visits/{patientId}", async (event) => {
  const { patientId } = event.params;
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();

  if (!after?.visits) {
    logger.warn("No visit data found for patient", patientId);
    return;
  }

  const newVisitKeys = Object.keys(after.visits);
  const oldVisitKeys = before?.visits ? Object.keys(before.visits) : [];
  const addedVisitKeys = newVisitKeys.filter(key => !oldVisitKeys.includes(key));

  if (addedVisitKeys.length === 0) {
    logger.log("No new visits for", patientId);
    return;
  }

  const patientSnap = await db.collection("patients").doc(patientId).get();
  const fcmToken = patientSnap.data()?.fcmToken;

  if (!fcmToken) {
    logger.warn("No FCM token for patient", patientId);
    return;
  }

  for (const visitId of addedVisitKeys) {
    try {
      await admin.messaging().send({
        token: fcmToken,
        notification: {
          title: "New Appointment",
          body: "Your appointment has been successfully booked"
        },
      });
      logger.log("Notification sent for visit", visitId);

      const markerRef = db.collection("visitNotifications")
        .doc(`${patientId}_${visitId}`);
      await markerRef.set({ timestamp: admin.firestore.FieldValue.serverTimestamp() });
    } catch (error) {
      logger.error("Error sending notification:", error);
    }
  }
});
