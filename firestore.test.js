const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_writer_1";
const BOB_UID = "bob_writer_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

test("Unauthenticated user cannot read projects or chapters", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("projects").get());
  await assertFails(unauthDb.collection("chapters").get());
});

test("Authenticated user can create and query own StoryProject", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      title: "El Eco de las Estrellas",
      genre: "Fantasía",
      premise: "Una aprendiz de cartógrafa descubre que los mapas antiguos alteran el continente.",
      structureTemplate: "THREE_ACTS",
      protagonistName: "Lyra Vance",
      protagonistSecret: "Ella misma borró del mapa la capital imperial.",
      centralConflict: "El Tribunal de Ceniza busca destruir el último atlas vivo.",
      targetWordCount: 50000,
      currentWordCount: 1200,
      isSharedBeta: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("projects").where("userId", "==", ALICE_UID).get()
  );
});

test("Cross-user isolation: Bob cannot read or update Alice's project", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("proj_alice").set({
      userId: ALICE_UID,
      title: "Novela Secreta",
      genre: "Thriller",
      premise: "Un misterio en alta mar.",
      structureTemplate: "THREE_ACTS",
      protagonistName: "Marco",
      protagonistSecret: "Es el impostor.",
      centralConflict: "Tormenta solar.",
      targetWordCount: 40000,
      currentWordCount: 0,
      isSharedBeta: false,
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("projects").doc("proj_alice").get());
  await assertFails(
    bobDb.collection("projects").doc("proj_alice").update({
      title: "Hackeado",
      updatedAt: new Date(),
    })
  );
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("projects").doc("proj_ghost").set({
      userId: ALICE_UID,
      title: "Libro con campo fantasma",
      genre: "Ciencia Ficción",
      premise: "Viaje interdimensional.",
      structureTemplate: "HERO_JOURNEY",
      protagonistName: "Kael",
      protagonistSecret: "Viene del futuro.",
      centralConflict: "Paradoja temporal.",
      targetWordCount: 60000,
      currentWordCount: 0,
      isSharedBeta: false,
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user fails query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("projects").get());
});
