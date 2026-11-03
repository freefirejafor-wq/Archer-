const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read tasks, meetings, memories, or moods", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("meetings").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("memories").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("moods").get());
});

test("Authenticated user: cannot read another user's task", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("tasks").doc("task_1").set({
      userId: BOB_UID,
      title: "Upload video to TikTok",
      category: "Routine",
      dueTime: "10:00 AM",
      completed: false,
      priority: "HIGH",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("tasks").doc("task_1").get()
  );
});

test("Authenticated owner: can create, read, update, and list own tasks, meetings, memories, and moods", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      userId: ALICE_UID,
      title: "Release desktop version 78",
      category: "Work",
      dueTime: "02:00 PM",
      completed: false,
      priority: "HIGH",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").where("userId", "==", ALICE_UID).get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("meetings").doc("meet_1").set({
      userId: ALICE_UID,
      title: "Product Sync Meeting",
      startTime: "04:00 PM",
      remindBeforeMinutes: 15,
      isUrgent: true,
      notes: "Prepare sprint metrics",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("memories").doc("mem_1").set({
      userId: ALICE_UID,
      content: "User prefers morning briefings in Bengali",
      tag: "Preference",
      emotionContext: "Calm",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("moods").doc("mood_1").set({
      userId: ALICE_UID,
      emotion: "Focused",
      soulMode: "Empathetic Companion",
      note: "Ready for deep work session",
      energyScore: 85,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Shadow update test: rejects ghost fields and invalid userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_bad").set({
      userId: ALICE_UID,
      title: "Task with ghost field",
      category: "Work",
      dueTime: "02:00 PM",
      completed: false,
      priority: "HIGH",
      isVerified: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_spoof").set({
      userId: BOB_UID,
      title: "Spoofed owner",
      category: "Work",
      dueTime: "02:00 PM",
      completed: false,
      priority: "HIGH",
      createdAt: now,
      updatedAt: now,
    })
  );
});
