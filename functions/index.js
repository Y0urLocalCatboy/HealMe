// functions/index.js

/**
* Firebase Cloud Functions entry point.
* This file initializes the Firebase Admin SDK and exports various functions
* for handling appointment reminders, visit notifications, chat messages,
* newsletter notifications, and cleaning old availabilities.
*/

const admin = require("firebase-admin");
const { logger } = require("firebase-functions");

admin.initializeApp();
// cd path to this
// cd firebase login
// cd firebase deploy --only functions: file_name

const sendAppointmentReminders = require("./reminders/sendAppointmentReminders");
const notifyNewVisit = require("./visits/notifyNewVisit");
const notifyNewChatMessage = require("./chat/notifyNewChatMessage");
const triggerNewsletterNotifications = require("./newsletter/triggerNewsletterNotifications");
const cleanOldAvailabilities = require("./past_appointments/cleanOldAvailabilities");

exports.sendAppointmentReminders = sendAppointmentReminders;
exports.notifyNewVisit = notifyNewVisit;
exports.notifyNewChatMessage = notifyNewChatMessage;
exports.triggerNewsletterNotifications = triggerNewsletterNotifications;
exports.cleanOldAvailabilities = cleanOldAvailabilities;
