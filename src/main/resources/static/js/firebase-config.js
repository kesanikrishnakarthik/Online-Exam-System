/**
 * Firebase Client Configuration & Cloud Firestore Integration
 * Project ID: online-exams-98fa6
 * College Online Examination System - DevOps Academic Project
 */

// Official Web Configuration for project online-exams-98fa6
const firebaseConfig = {
    apiKey: "AIzaSyDA7bq2-FRfIjO_ldu2r4tYQ4Wp_KmwVTg",
    authDomain: "online-exams-98fa6.firebaseapp.com",
    projectId: "online-exams-98fa6",
    storageBucket: "online-exams-98fa6.firebasestorage.app",
    messagingSenderId: "204869742765",
    appId: "1:204869742765:web:22b7939b4a550f40a55624",
    measurementId: "G-NTKWQRBQT2"
};

// Global Firebase State
window.FirebaseState = {
    initialized: false,
    app: null,
    db: null,
    auth: null,
    analytics: null,
    statusText: "Initializing..."
};

// Initialize Firebase using compatible CDN SDKs
(function initializeFirebaseIntegration() {
    try {
        if (typeof firebase !== 'undefined') {
            // Check if already initialized
            if (!firebase.apps || !firebase.apps.length) {
                window.FirebaseState.app = firebase.initializeApp(firebaseConfig);
            } else {
                window.FirebaseState.app = firebase.app();
            }

            if (firebase.firestore) {
                window.FirebaseState.db = firebase.firestore();
            }

            if (firebase.auth) {
                window.FirebaseState.auth = firebase.auth();
            }

            if (firebase.analytics) {
                try {
                    window.FirebaseState.analytics = firebase.analytics();
                } catch (e) {
                    console.log("Firebase Analytics initialized in standard mode:", e.message);
                }
            }

            window.FirebaseState.initialized = true;
            window.FirebaseState.statusText = "Connected";
            console.log("Firebase initialized successfully for project:", firebaseConfig.projectId);
        } else {
            window.FirebaseState.statusText = "Standby (Local Fallback)";
            console.log("Firebase SDK loading in fallback mode.");
        }
    } catch (err) {
        console.warn("Firebase initialization notice:", err.message);
        window.FirebaseState.statusText = "Offline / Local Mode";
    }

    // Attach visual badge when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', renderFirebaseBadge);
    } else {
        renderFirebaseBadge();
    }
})();

/**
 * Renders a visual Firebase Connection Badge in the UI header
 */
function renderFirebaseBadge() {
    if (document.getElementById('firebase-status-badge')) return;

    const badge = document.createElement('div');
    badge.id = 'firebase-status-badge';
    badge.style.display = 'inline-flex';
    badge.style.alignItems = 'center';
    badge.style.gap = '6px';
    badge.style.padding = '5px 12px';
    badge.style.borderRadius = '20px';
    badge.style.fontSize = '12px';
    badge.style.fontWeight = '600';
    badge.style.letterSpacing = '0.3px';
    badge.style.backgroundColor = 'rgba(255, 255, 255, 0.15)';
    badge.style.backdropFilter = 'blur(6px)';
    badge.style.color = '#ffffff';
    badge.style.border = '1px solid rgba(255, 255, 255, 0.25)';
    badge.style.margin = '0 10px';

    const isConnected = window.FirebaseState.initialized;
    badge.innerHTML = `
        <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background-color:${isConnected ? '#22c55e' : '#f59e0b'}; box-shadow:0 0 8px ${isConnected ? '#22c55e' : '#f59e0b'};"></span>
        <span>Firebase: ${firebaseConfig.projectId}</span>
    `;

    // Try finding the navigation header to inject
    const header = document.querySelector('header');
    if (header) {
        const nav = header.querySelector('nav') || header;
        nav.appendChild(badge);
    }
}

/**
 * Syncs exam attempt record to Cloud Firestore
 */
async function syncExamAttemptToFirestore(attemptData) {
    if (!window.FirebaseState.db) {
        console.log("Local attempt recorded (Firestore standby):", attemptData);
        return;
    }
    try {
        await window.FirebaseState.db.collection("examAttempts").add({
            ...attemptData,
            syncedAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log("Exam attempt synced to Cloud Firestore.");
    } catch (e) {
        console.warn("Firestore attempt sync skipped:", e.message);
    }
}

/**
 * Syncs final exam result to Cloud Firestore
 */
async function syncExamResultToFirestore(resultData) {
    if (!window.FirebaseState.db) {
        console.log("Local result recorded (Firestore standby):", resultData);
        return;
    }
    try {
        await window.FirebaseState.db.collection("results").add({
            ...resultData,
            syncedAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log("Exam result synced to Cloud Firestore.");
    } catch (e) {
        console.warn("Firestore result sync skipped:", e.message);
    }
}

/**
 * Logs anti-cheating monitoring events to Cloud Firestore
 */
async function logViolationToFirestore(violationData) {
    if (!window.FirebaseState.db) {
        console.log("Local violation logged:", violationData);
        return;
    }
    try {
        await window.FirebaseState.db.collection("examViolations").add({
            ...violationData,
            timestamp: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log("Anti-cheating violation recorded in Cloud Firestore.");
    } catch (e) {
        console.warn("Firestore violation sync skipped:", e.message);
    }
}

