import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import {
  createGreeting,
  deleteGreeting,
  fetchGreetings,
  type ApiError,
  type Greeting,
  type NewGreeting,
} from "../../api/greetingsApi";

export interface GreetingsState {
  items: Greeting[];
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
}

const initialState: GreetingsState = {
  items: [],
  status: "idle",
  error: null,
};

function toErrorMessage(error: unknown): string {
  const apiError = error as ApiError;
  return apiError?.message ?? "Something went wrong talking to the backend.";
}

export const loadGreetings = createAsyncThunk<Greeting[]>(
  "greetings/load",
  async () => fetchGreetings(),
);

export const addGreeting = createAsyncThunk<Greeting, NewGreeting>(
  "greetings/add",
  async (payload) => createGreeting(payload),
);

export const removeGreeting = createAsyncThunk<number, number>(
  "greetings/remove",
  async (id) => {
    await deleteGreeting(id);
    return id;
  },
);

const greetingsSlice = createSlice({
  name: "greetings",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(loadGreetings.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(loadGreetings.fulfilled, (state, action: PayloadAction<Greeting[]>) => {
        state.status = "succeeded";
        state.items = action.payload;
      })
      .addCase(loadGreetings.rejected, (state, action) => {
        state.status = "failed";
        state.error = toErrorMessage(action.error);
      })
      .addCase(addGreeting.fulfilled, (state, action: PayloadAction<Greeting>) => {
        state.items.push(action.payload);
      })
      .addCase(addGreeting.rejected, (state, action) => {
        state.error = toErrorMessage(action.error);
      })
      .addCase(removeGreeting.fulfilled, (state, action: PayloadAction<number>) => {
        state.items = state.items.filter((g) => g.id !== action.payload);
      });
  },
});

export default greetingsSlice.reducer;
