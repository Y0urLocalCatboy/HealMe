const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentWritten, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Scheduled appointment reminders
exports.sendAppointmentReminders = onSchedule("every 1 hours", async () => {
  const now = Date.now() / 1000;
  const oneDayLater = now + 86400;

  const visitsSnapshot = await db.collection("visits").get();

  for (const doc of visitsSnapshot.docs) {
    const patientId = doc.id;
    const visitsMap = doc.data().visits || {};

    for (const visitId in visitsMap) {
      const visit = visitsMap[visitId];

      if (visit.timestamp >= now && visit.timestamp <= oneDayLater) {
        const markerRef = db.collection("visitNotifications")
          .doc(`${patientId}_${visitId}`);
        const markerDoc = await markerRef.get();

        if (markerDoc.exists) {
          logger.log("Reminder already sent for visit", visitId);
          continue;
        }

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

          await markerRef.set({
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
          });
        } else {
          logger.warn("No FCM token for patient", patientId);
        }
      }
    }
  }
});

// New visit notification
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
      await markerRef.set({
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });
    } catch (error) {
      logger.error("Error sending notification:", error);
    }
  }
});

exports.notifyNewChatMessage = onDocumentUpdated("messages/{chatId}", async (event) => {
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();

  if (!after?.weeklymessages || !Array.isArray(after.weeklymessages)) {
    logger.warn("No weekly messages array found");
    return;
  }

  const beforeMessages = before?.weeklymessages || [];
  const afterMessages = after.weeklymessages;

  if (afterMessages.length <= beforeMessages.length) {
    logger.log("No new message detected");
    return;
  }

  const newMessage = afterMessages[afterMessages.length - 1];
  const receiverId = after.receiverId;

  const userDoc = await db.collection("patients").doc(receiverId).get();
  const fcmToken = userDoc.data()?.fcmToken;

  if (!fcmToken) {
    logger.warn("No FCM token for receiver", receiverId);
    return;
  }

  await admin.messaging().send({
    token: fcmToken,
    notification: {
      title: "New message",
      body: newMessage.content || "You have received a new message"
    },
    android: {
      priority: "high"
    }
  });

  logger.log("Notification sent to", receiverId);
});
