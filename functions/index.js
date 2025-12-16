/**
 * Firebase Cloud Functions
 *
 * This file contains Cloud Functions to handle notifications when a new
 * review is added to a hunt. It uses Firestore triggers and FCM messaging.
 *
 * Required dependencies:
 *   - firebase-functions
 *   - firebase-admin
 */

const { setGlobalOptions } = require("firebase-functions/v2");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

// Configure Firestore emulator if running locally
if (process.env.FIRESTORE_EMULATOR_HOST) {
  const [host, port] = process.env.FIRESTORE_EMULATOR_HOST.split(":");
  admin.firestore().useEmulator(host, parseInt(port, 10));
}

/**
 * Triggered when a new review is added to the `huntsReviews` collection.
 * Sends a notification to the owner of the hunt.
 *
 * @param {Object} snap Firestore snapshot of the newly created document.
 * @param {Object} context Cloud Functions context object, contains params like reviewId.
 */
exports.sendReviewNotification = onDocumentCreated(
  "huntsReviews/{reviewId}",
  async (event) => {
    const review = event.data?.data();
    const reviewId = event.params.reviewId;

    if (!review) {
      console.log("Missing review payload for", reviewId);
      await logDebug({ status: "missing_review", reviewId });
      return;
    }

    const huntId = review.huntId;

    console.log("New review created:", reviewId, "for hunt:", huntId);

    // 1. Get the hunt document
    const huntRef = admin.firestore().collection("hunts").doc(huntId);
    const huntDoc = await huntRef.get();

    if (!huntDoc.exists) {
      console.log("Hunt not found:", huntId);
      await logDebug({
        status: "hunt_not_found",
        reviewId,
        huntId,
      });
      return;
    }

    const hunt = huntDoc.data();
    const ownerId = hunt.authorId;

    console.log("Hunt owner:", ownerId);

    // 2. Get the owner's profile to retrieve FCM token
    const profileRef = admin.firestore().collection("profiles").doc(ownerId);
    const userDoc = await profileRef.get();

    const token = userDoc.data()?.author?.fcmToken;

    if (!token) {
      console.log("No FCM token for user:", ownerId);
      await logDebug({
        status: "no_token",
        reviewId,
        huntId,
        ownerId,
      });
      return;
    }

    console.log("Sending notification to:", token);

    // 3. Build the FCM message
    const message = {
      token: token,
      notification: {
        title: "New review!",
        body: `Your hunt '${hunt.title}' received a new review: ${review.comment}`,
      },
      data: {
        huntId: huntId,
        reviewId: reviewId,
      },
    };

    // 4. Send FCM notification
    try {
      await admin.messaging().send(message);
      console.log("Notification sent.");

      await logDebug({
        status: "sent",
        reviewId,
        huntId,
        ownerId,
        token,
      });
    } catch (err) {
      console.error("Error sending notification:", err);
      await logDebug({
        status: "send_error",
        reviewId,
        huntId,
        ownerId,
        error: err.message,
      });
    }
  }
);

/**
 * Helper function to log debug information in Firestore.
 *
 * @param {Object} data Data to log in `debug_notifications` collection.
 * @returns {Promise<FirebaseFirestore.DocumentReference>} Document reference of the added log.
 */
async function logDebug(data) {
  return admin
    .firestore()
    .collection("debug_notifications")
    .add({
      ...data,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });
}

// Set maximum concurrent instances for all functions to mitigate traffic spikes
setGlobalOptions({ maxInstances: 10 });
