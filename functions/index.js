const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// 🔔 Scheduled Reminder Function
exports.sendAppointmentReminders = onSchedule("every 1 hours", async (event) => {
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

        if (markerDoc.exists) {
          logger.log(`🔁 Already sent reminder for visit ${visitId}`);
          continue;
        }

        const patientSnap = await db.collection("patients").doc(patientId).get();
        const token = patientSnap.data()?.fcmToken;

        if (token) {
          await admin.messaging().send({
            token,
            notification: {
              title: "📅 Upcoming Appointment",
              body: "Reminder: You have an appointment scheduled for tomorrow!",
            },
          });
          logger.log(`✅ Reminder sent to ${patientId} for visit ${visitId}`);

          // Save marker to avoid resending
          await markerRef.set({
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
          });
        } else {
          logger.warn(`⚠️ No FCM token for patient ${patientId}`);
        }
      }
    }
  }
});

// 🔔 Triggered Notification Function
exports.notifyNewVisit = onDocumentWritten("visits/{patientId}", async (event) => {
  const { patientId } = event.params;
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();

  if (!after || !after.visits) {
    logger.warn(`⚠️ No visit data found for patient ${patientId}`);
    return;
  }

  const newVisitKeys = Object.keys(after.visits);
  const oldVisitKeys = before?.visits ? Object.keys(before.visits) : [];
  const addedVisitKeys = newVisitKeys.filter((key) => !oldVisitKeys.includes(key));

  if (addedVisitKeys.length === 0) {
    logger.log(`ℹ️ No new visits for ${patientId}`);
    return;
  }

  const patientSnap = await db.collection("patients").doc(patientId).get();
  const fcmToken = patientSnap.data()?.fcmToken;

  if (!fcmToken) {
    logger.warn(`⚠️ No FCM token for patient ${patientId}`);
    return;
  }

  for (const visitId of addedVisitKeys) {
    try {
      await admin.messaging().send({
        token: fcmToken,
        notification: {
          title: "🩺 Nowa wizyta",
          body: "Twoja wizyta została pomyślnie zarezerwowana.",
        },
      });
      logger.log(`✅ Notification sent for new visit(s): ${visitId} to ${patientId}`);

      const markerRef = db.collection("visitNotifications").doc(`${patientId}_${visitId}`);
      await markerRef.set({
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });
    } catch (error) {
      logger.error(`❌ Error sending notification for ${visitId}:`, error);
    }
  }
});
