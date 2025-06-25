// functions/reminders/sendAppointmentReminders.js

/**
* Firebase Cloud Function to send appointment reminders to patients.
* It checks for appointments scheduled within the next 24 hours and sends
* notifications to patients with valid FCM tokens.
* The function runs every hour.
*/

const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const { logger } = require("firebase-functions");
const db = admin.firestore();

module.exports = onSchedule("every 1 hours", async () => {
  const now = Date.now() / 1000;
  const oneDayLater = now + 86400;

  const visitsSnapshot = await db.collection("visits").get();

  for (const doc of visitsSnapshot.docs) {
    const patientId = doc.id;
    const visitsMap = doc.data().visits || {};

    for (const visitId in visitsMap) {
      const visit = visitsMap[visitId];
      if (visit.timestamp >= now && visit.timestamp <= oneDayLater) {
        const markerRef = db.collection("visitNotifications").doc(`${patientId}_${visitId}`);
        const markerDoc = await markerRef.get();

        if (markerDoc.exists) continue;

        const patientSnap = await db.collection("patients").doc(patientId).get();
        const token = patientSnap.data()?.fcmToken;

        if (token) {
          await admin.messaging().send({
            token,
            notification: {
              title: "Upcoming Appointment",
              body: "You have an appointment scheduled for tomorrow"
            },
          });
          logger.log("Reminder sent to", patientId);
          await markerRef.set({ timestamp: admin.firestore.FieldValue.serverTimestamp() });
        }
      }
    }
  }
});
