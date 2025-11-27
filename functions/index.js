/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
if (!admin.apps.length) {
  admin.initializeApp();
}
if (process.env.FIRESTORE_EMULATOR_HOST) {
  const [host, port] = process.env.FIRESTORE_EMULATOR_HOST.split(":");
  admin.firestore().useEmulator(host, parseInt(port, 10));
}
exports.sendReviewNotification = functions.firestore
  .document("huntsReviews/{reviewId}")
  .onCreate(async (snap, context) => {

    const review = snap.data();
    const huntId = review.huntId;
    const reviewId = context.params.reviewId;

    console.log("New review created:", reviewId, "for hunt:", huntId);

    // 1. Get hunt
    const huntRef = admin.firestore().collection("hunts").doc(huntId);
    const huntDoc = await huntRef.get();

    if (!huntDoc.exists) {
      console.log("Hunt not found:", huntId);
      await logDebug({
        status: "hunt_not_found",
        reviewId,
        huntId
      });
      return;
    }

    const hunt = huntDoc.data();
    const ownerId = hunt.authorId;

    console.log("Hunt owner:", ownerId);

    // 2. Get owner profile
    const profileRef = admin.firestore().collection("profiles").doc(ownerId);
    const userDoc = await profileRef.get();

    const token = userDoc.data()?.author?.fcmToken;

    if (!token) {
      console.log("No FCM token for user:", ownerId);
      await logDebug({
        status: "no_token",
        reviewId,
        huntId,
        ownerId
      });
      return;
    }

    console.log("Sending notification to:", token);

    // 3. Build message
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
  });


// 🔧 Helper function to log debug output for emulator tests
async function logDebug(data) {
  return admin.firestore().collection("debug_notifications").add({
    ...data,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
  });
}

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
