const admin = require("firebase-admin");
const { logger } = require("firebase-functions");

admin.initializeApp();

const sendAppointmentReminders = require("./reminders/sendAppointmentReminders");
const notifyNewVisit = require("./visits/notifyNewVisit");
const notifyNewChatMessage = require("./chat/notifyNewChatMessage");
const triggerNewsletterNotifications = require("./newsletter/triggerNewsletterNotifications");

exports.sendAppointmentReminders = sendAppointmentReminders;
exports.notifyNewVisit = notifyNewVisit;
exports.notifyNewChatMessage = notifyNewChatMessage;
exports.triggerNewsletterNotifications = triggerNewsletterNotifications;