import { describe, expect, it } from "vitest";
import reducer, { addGreeting, loadGreetings, removeGreeting } from "./greetingsSlice";
import type { Greeting } from "../../api/greetingsApi";

const sample: Greeting = {
  id: 1,
  author: "Srikanth",
  message: "Hello, world!",
  createdAt: "2026-01-01T00:00:00Z",
};

describe("greetingsSlice reducer", () => {
  it("returns the initial state", () => {
    const state = reducer(undefined, { type: "@@INIT" });
    expect(state).toEqual({ items: [], status: "idle", error: null });
  });

  it("sets status to loading on loadGreetings.pending", () => {
    const state = reducer(undefined, loadGreetings.pending("requestId", undefined));
    expect(state.status).toBe("loading");
    expect(state.error).toBeNull();
  });

  it("stores the greetings on loadGreetings.fulfilled", () => {
    const state = reducer(
      undefined,
      loadGreetings.fulfilled([sample], "requestId", undefined),
    );
    expect(state.status).toBe("succeeded");
    expect(state.items).toEqual([sample]);
  });

  it("records an error message on loadGreetings.rejected", () => {
    const state = reducer(
      undefined,
      loadGreetings.rejected(new Error("network down"), "requestId", undefined),
    );
    expect(state.status).toBe("failed");
    expect(state.error).toBe("network down");
  });

  it("appends the new greeting on addGreeting.fulfilled", () => {
    const initial = reducer(undefined, loadGreetings.fulfilled([], "id", undefined));
    const state = reducer(
      initial,
      addGreeting.fulfilled(sample, "requestId", { author: "Srikanth", message: "Hello, world!" }),
    );
    expect(state.items).toEqual([sample]);
  });

  it("removes the greeting on removeGreeting.fulfilled", () => {
    const initial = reducer(undefined, loadGreetings.fulfilled([sample], "id", undefined));
    const state = reducer(initial, removeGreeting.fulfilled(sample.id, "requestId", sample.id));
    expect(state.items).toEqual([]);
  });
});
