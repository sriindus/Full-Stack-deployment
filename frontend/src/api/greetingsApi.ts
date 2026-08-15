export interface Greeting {
  id: number;
  author: string;
  message: string;
  createdAt: string;
}

export interface NewGreeting {
  author: string;
  message: string;
}

export interface ApiError {
  status: number;
  message: string;
  fieldErrors?: Record<string, string>;
}

// Injected at build/runtime via Vite env (see .env.example / docker-compose).
// Falls back to the local Spring Boot dev server.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const GREETINGS_URL = `${BASE_URL}/api/v1/greetings`;

async function parseErrorResponse(response: Response): Promise<ApiError> {
  try {
    const body = await response.json();
    return {
      status: response.status,
      message: body.message ?? response.statusText,
      fieldErrors: body.fieldErrors,
    };
  } catch {
    return { status: response.status, message: response.statusText };
  }
}

export async function fetchGreetings(): Promise<Greeting[]> {
  const response = await fetch(GREETINGS_URL);
  if (!response.ok) {
    throw await parseErrorResponse(response);
  }
  return response.json();
}

export async function createGreeting(payload: NewGreeting): Promise<Greeting> {
  const response = await fetch(GREETINGS_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw await parseErrorResponse(response);
  }
  return response.json();
}

export async function deleteGreeting(id: number): Promise<void> {
  const response = await fetch(`${GREETINGS_URL}/${id}`, { method: "DELETE" });
  if (!response.ok && response.status !== 204) {
    throw await parseErrorResponse(response);
  }
}
