// functions/newsletter/triggerNewsletterNotifications.js

const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();

module.exports = onSchedule("every 5 minutes", async () => {
  const now = Math.floor(Date.now() / 1000);
  logger.log("🔄 Newsletter function triggered at Unix:", now);

  try {
    const snapshot = await db.collection("newsletters")
      .where("timestamp", "<=", now + 60) // Allowing a 1-minute window
      .get();

    logger.log(`📦 Found ${snapshot.size} newsletter(s) ready to send`);

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const { message, targetRoles, timestamp } = data;

      logger.log(`📨 Processing newsletter ID: ${doc.id}`);
      logger.log(`📅 Scheduled timestamp: ${timestamp}, current time: ${now}`);

      if (!message || !targetRoles || !Array.isArray(targetRoles)) {
        logger.warn("⚠️ Invalid newsletter format in document:", doc.id);
        continue;
      }

      for (const role of targetRoles) {
        const collectionName = role === "doctors" ? "doctors" : "patients";
        const usersSnapshot = await db.collection(collectionName).get();

        logger.log(`👥 Preparing to send to ${usersSnapshot.size} ${collectionName}`);

        for (const userDoc of usersSnapshot.docs) {
          const token = userDoc.data()?.fcmToken;

          if (token) {
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
              logger.log(`✅ Newsletter sent to ${collectionName} user: ${userDoc.id}`);
            } catch (error) {
              logger.error(`❌ Failed to send to ${userDoc.id}`, error);
            }
          } else {
            logger.warn(`⚠️ Missing FCM token for ${collectionName} user: ${userDoc.id}`);
          }
        }
      }

      await db.collection("newsletters").doc(doc.id).delete();
      logger.log("🧹 Cleaned up processed newsletter:", doc.id);
    }

    logger.log("✅ Newsletter dispatch completed");
  } catch (err) {
    logger.error("🔥 Newsletter dispatch failed:", err.message);
  }
});
