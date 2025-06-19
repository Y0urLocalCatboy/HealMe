const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const { logger } = require("firebase-functions");
const db = admin.firestore();

module.exports = onDocumentWritten("visits/{patientId}", async (event) => {
  const { patientId } = event.params;
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();

  if (!after?.visits) return;

  const newVisitKeys = Object.keys(after.visits);
  const oldVisitKeys = before?.visits ? Object.keys(before.visits) : [];
  const addedVisitKeys = newVisitKeys.filter(key => !oldVisitKeys.includes(key));

  if (addedVisitKeys.length === 0) return;

  const patientSnap = await db.collection("patients").doc(patientId).get();
  const fcmToken = patientSnap.data()?.fcmToken;

  if (!fcmToken) return;

  for (const visitId of addedVisitKeys) {
    try {
      await admin.messaging().send({
        token: fcmToken,
        notification: {
          title: "New Appointment",
          body: "Your appointment has been successfully booked"
        },
      });

      await db.collection("visitNotifications").doc(`${patientId}_${visitId}`).set({
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });
    } catch (error) {
      logger.error("Error sending notification:", error);
    }
  }
});
