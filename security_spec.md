# Security Specification — Archer AI Firestore Rules

## 1. Data Invariants
1. **User Isolation**: Every document under `/users/{userId}/tasks/{taskId}`, `/users/{userId}/meetings/{meetingId}`, `/users/{userId}/memories/{memoryId}`, and `/users/{userId}/moods/{moodId}` belongs strictly to `userId` (`request.auth.uid == userId` and `data.userId == request.auth.uid`).
2. **Immutable Ownership & Creation Time**: `userId` and `createdAt` can never be mutated during an `update`.
3. **Strict Schema & Bounds**: All string fields have bounded lengths (`.size() >= 1 && .size() <= max`), booleans and integers are type-checked, and timestamps satisfy `is timestamp && <= request.time`.
4. **No Ghost Fields**: `incoming().keys().hasOnly(...)` on create and `incoming().diff(existing()).affectedKeys().hasOnly(...)` on update block shadow/injected fields.

## 2. Dirty Dozen Payloads
1. Unauthenticated read/list on `/users/alice/tasks`.
2. Authenticated cross-user read (`/users/bob/tasks/t1` by `alice`).
3. Create task with mismatched `userId` (`userId: "bob"` under `/users/alice/tasks/t1`).
4. Create task with ghost field (`isAdmin: true`).
5. Create task with empty title (`title: ""`) or oversized title (`> 300` chars).
6. Update task mutating `createdAt` or `userId`.
7. Create meeting with invalid `remindBeforeMinutes` (`-5` or `> 1440`).
8. Create memory with invalid tag or oversized content (`> 2000` chars).
9. Create mood log with out-of-range `energyScore` (`0` or `105`).
10. Update memory injecting unauthorized key (`systemVerified: true`).
11. Poisoned document ID (`> 128` chars or special characters).
12. Client timestamp spoofing in the future (`createdAt > request.time`).
