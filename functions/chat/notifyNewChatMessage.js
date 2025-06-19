const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const { logger } = require("firebase-functions");
const db = admin.firestore();

module.exports = onDocumentUpdated("messages/{chatId}", async (event) => {
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();

  if (!after?.weeklymessages || !Array.isArray(after.weeklymessages)) return;

  const beforeMessages = before?.weeklymessages || [];
  const afterMessages = after.weeklymessages;

  if (afterMessages.length <= beforeMessages.length) return;

  const newMessage = afterMessages[afterMessages.length - 1];
  const receiverId = after.receiverId;

  const userDoc = await db.collection("patients").doc(receiverId).get();
  const fcmToken = userDoc.data()?.fcmToken;

  if (!fcmToken) return;

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
