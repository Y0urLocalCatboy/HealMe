const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const { logger } = require("firebase-functions");
const db = admin.firestore();

/**
 * Helper function to find a user in either patients or doctors collection.
 * @param {string} userId The ID of the user to find.
 * @return {Promise<object|null>} The user data or null if not found.
 */
async function findUser(userId) {
  if (!userId) return null;
  const patientDoc = await db.collection("patients").doc(userId).get();
  if (patientDoc.exists) {
    return patientDoc.data();
  }
  const doctorDoc = await db.collection("doctors").doc(userId).get();
  if (doctorDoc.exists) {
    return doctorDoc.data();
  }
  return null;
}

module.exports = onDocumentUpdated("messages/{chatId}", async (event) => {
  const afterData = event.data?.after?.data();
  const beforeData = event.data?.before?.data();

  if (!afterData?.weeklymessages || !Array.isArray(afterData.weeklymessages)) {
    logger.log("No weeklymessages array found or it's not an array.");
    return;
  }

  const beforeMessages = beforeData?.weeklymessages || [];
  const afterMessages = afterData.weeklymessages;

  if (afterMessages.length <= beforeMessages.length) {
    logger.log("No new message detected.");
    return;
  }

  const newMessage = afterMessages[afterMessages.length - 1];

  if (!newMessage || !newMessage.senderId) {
    logger.error("New message is malformed or missing senderId.", newMessage);
    return;
  }

  const messageSenderId = newMessage.senderId;
  const chatParticipant1 = afterData.senderId;
  const chatParticipant2 = afterData.receiverId;

  if (!chatParticipant1 || !chatParticipant2) {
      logger.error("Chat document is missing top-level senderId or receiverId.");
      return;
  }

  let notificationReceiverId;
  if (messageSenderId === chatParticipant1) {
    notificationReceiverId = chatParticipant2;
  } else if (messageSenderId === chatParticipant2) {
    notificationReceiverId = chatParticipant1;
  } else {
    logger.error(`Message sender ${messageSenderId} is not a participant in this chat.`);
    return;
  }

  const [receiverData, senderData] = await Promise.all([
    findUser(notificationReceiverId),
    findUser(messageSenderId),
  ]);

  if (!receiverData || !receiverData.fcmToken) {
    logger.log(`Receiver ${notificationReceiverId} not found or has no FCM token.`);
    return;
  }
  const fcmToken = receiverData.fcmToken;

  const senderName = senderData ? `${senderData.name} ${senderData.surname}`.trim() : "Someone";
  const notificationBody = newMessage.content || (newMessage.imageUrl ? "Sent you an image" : "You have a new message");

  const payload = {
    token: fcmToken,
    notification: {
      title: `New message from ${senderName}`,
      body: notificationBody,
    },
    android: {
      priority: "high",
    },
    data: {
        senderId: messageSenderId,
        chatId: event.params.chatId
    }
  };

  try {
    await admin.messaging().send(payload);
    logger.log(`Notification sent successfully to receiver ${notificationReceiverId}.`);
  } catch (error) {
    logger.error(`Error sending notification to ${notificationReceiverId}:`, error);
  }
});