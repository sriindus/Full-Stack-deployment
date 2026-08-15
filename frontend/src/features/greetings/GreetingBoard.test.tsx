import { configureStore } from "@reduxjs/toolkit";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { beforeEach, describe, expect, it, vi } from "vitest";
import GreetingBoard from "./GreetingBoard";
import greetingsReducer from "./greetingsSlice";
import * as greetingsApi from "../../api/greetingsApi";

function renderWithStore() {
  const store = configureStore({ reducer: { greetings: greetingsReducer } });
  return render(
    <Provider store={store}>
      <GreetingBoard />
    </Provider>,
  );
}

describe("GreetingBoard", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("loads and displays greetings from the API on mount", async () => {
    vi.spyOn(greetingsApi, "fetchGreetings").mockResolvedValue([
      { id: 1, author: "Srikanth", message: "Hello, world!", createdAt: "2026-01-01T00:00:00Z" },
    ]);

    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/Srikanth/)).toBeInTheDocument();
      expect(screen.getByText(/Hello, world!/)).toBeInTheDocument();
    });
  });

  it("submits the form and adds a new greeting to the list", async () => {
    vi.spyOn(greetingsApi, "fetchGreetings").mockResolvedValue([]);
    vi.spyOn(greetingsApi, "createGreeting").mockResolvedValue({
      id: 2,
      author: "Alice",
      message: "Hi team",
      createdAt: "2026-01-02T00:00:00Z",
    });

    renderWithStore();

    await waitFor(() => expect(screen.getByText(/No greetings yet/)).toBeInTheDocument());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText("Author"), "Alice");
    await user.clear(screen.getByLabelText("Message"));
    await user.type(screen.getByLabelText("Message"), "Hi team");
    await user.click(screen.getByRole("button", { name: /post greeting/i }));

    await waitFor(() => {
      expect(screen.getByText(/Alice/)).toBeInTheDocument();
      expect(screen.getByText(/Hi team/)).toBeInTheDocument();
    });
    expect(greetingsApi.createGreeting).toHaveBeenCalledWith({ author: "Alice", message: "Hi team" });
  });

  it("shows an error message when loading greetings fails", async () => {
    vi.spyOn(greetingsApi, "fetchGreetings").mockRejectedValue({
      status: 500,
      message: "backend is unreachable",
    });

    renderWithStore();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("backend is unreachable");
    });
  });
});
