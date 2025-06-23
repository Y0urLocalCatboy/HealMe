const { initializeApp, applicationDefault } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  initializeApp({
    credential: applicationDefault(),
  });
}

const db = getFirestore();

exports.cleanOldAvailabilities = onSchedule("every 30 minutes", async (event) => {
  console.log("=== Starting availability cleanup ===");

  const now = Math.floor(Date.now() / 1000);
  console.log("Current epoch time:", now);

  const snapshot = await db.collection("availability").get();
  console.log(`Found ${snapshot.size} documents in 'availability' collection.`);

  for (const doc of snapshot.docs) {
    console.log(`Processing document: ${doc.id}`);
    const data = doc.data();
    const availabilityMap = data.weeklyAvailability;

    if (!availabilityMap) {
      console.log(`No 'weeklyAvailability' field in document: ${doc.id}`);
      continue;
    }

    const updatedAvailability = {};
    let modified = false;

    for (const [timestampKey, slotData] of Object.entries(availabilityMap)) {
      const timestamp = parseInt(timestampKey, 10);
      const status = slotData?.status;

      console.log(`Slot: ${timestampKey}, Status: ${status}`);

      if (!isNaN(timestamp)) {
        if (timestamp >= now) {
          updatedAvailability[timestampKey] = slotData;
        } else {
          console.log(`-> Deleting past slot: ${timestampKey}`);
          modified = true;
        }
      } else {
        console.log(`-> Deleting invalid slot: ${timestampKey}`);
        modified = true;
      }
    }

    if (modified) {
      console.log(
        `Updating document: ${doc.id} with cleaned availability`,
        updatedAvailability
      );

      await db.collection("availability").doc(doc.id).set(
        {
          weeklyAvailability: updatedAvailability,
        },
        { merge: false }
      );
    }
  }

  console.log("=== Availability cleanup complete ===");
});
