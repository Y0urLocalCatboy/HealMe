const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

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
    android: { priority: "high" }
  });

  logger.log("Notification sent to", receiverId);
});
