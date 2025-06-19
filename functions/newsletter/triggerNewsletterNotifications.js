// functions/newsletter/triggerNewsletterNotifications.js

const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();

module.exports = onSchedule("every 1 minutes", async () => {
  const now = Math.floor(Date.now() / 1000);
  const buffer = 60;
  const maxTimestamp = now + buffer;

  logger.log("🔄 Newsletter function triggered at Unix:", now);

  try {
    const snapshot = await db.collection("newsletters")
      .where("timestamp", "<=", maxTimestamp)
      .get();

    logger.log(`📦 Found ${snapshot.size} newsletter(s) ready to send (≤ ${maxTimestamp})`);

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const { message, targetRoles, timestamp } = data;

      logger.log(`📨 Processing newsletter: ${doc.id}`);
      logger.log(`🕒 Timestamp: ${timestamp}, Now: ${now}, Max allowed: ${maxTimestamp}`);

      if (!message || !targetRoles || !Array.isArray(targetRoles)) {
        logger.warn(`⚠️ Invalid newsletter format in doc: ${doc.id}`);
        continue;
      }

      for (const role of targetRoles) {
        const collectionName = role === "doctors" ? "doctors" : "patients";
        const usersSnapshot = await db.collection(collectionName).get();

        logger.log(`📤 Sending to ${usersSnapshot.size} ${collectionName}`);

        for (const userDoc of usersSnapshot.docs) {
          const token = userDoc.data()?.fcmToken;

          if (!token) {
            logger.warn(`⚠️ Missing FCM token for ${collectionName} user: ${userDoc.id}`);
            continue;
          }

          try {
            await admin.messaging().send({
              token,
              notification: {
                title: "Newsletter",
                body: message,
              },
              android: {
                priority: "high",
              },
            });

            logger.log(`✅ Sent to ${collectionName} user: ${userDoc.id}`);
          } catch (err) {
            logger.error(`❌ Failed to send to ${collectionName} user ${userDoc.id}:`, err.message);
          }
        }
      }

      await db.collection("newsletters").doc(doc.id).delete();
      logger.log(`🧹 Deleted processed newsletter: ${doc.id}`);
    }

    logger.log("✅ Newsletter dispatch function completed");
  } catch (err) {
    logger.error("🔥 Newsletter dispatch function failed:", err.message);
  }
});
