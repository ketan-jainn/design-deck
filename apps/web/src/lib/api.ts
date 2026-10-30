import type { Category, Grade, Profile, ProgressSummary, Question } from "./types";

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  profile: Profile;
};

type Method = "GET" | "POST" | "PATCH";

async function request<T>(path: string, method: Method = "GET", body?: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    credentials: "same-origin",
    headers: body ? { "content-type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (
    (res.status === 401 || res.status === 403) &&
    typeof window !== "undefined" &&
    !location.pathname.startsWith("/auth")
  ) {
    location.replace("/auth");
  }
  if (!res.ok) {
    const message = await res.text();
    throw new Error(message || `Request failed (${res.status})`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  signup: (data: { email: string; password: string; displayName?: string }) =>
    request<AuthResponse>("/api/auth/signup", "POST", data),
  login: (data: { email: string; password: string }) =>
    request<AuthResponse>("/api/auth/login", "POST", data),
  logout: () => request<{ ok: true }>("/api/auth/logout", "POST"),
  forgotPassword: (data: { email: string }) =>
    request<{ ok: true }>("/api/auth/forgot-password", "POST", data),
  resetPassword: (data: { token: string; password: string }) =>
    request<{ ok: true }>("/api/auth/reset-password", "POST", data),
  me: () => request<Profile>("/api/me"),
  updateMe: (data: { displayName: string; dailyGoal: number }) =>
    request<Profile>("/api/me", "PATCH", data),
  categories: () => request<Category[]>("/api/categories"),
  questions: (params: { topic?: string; q?: string } = {}) => {
    const search = new URLSearchParams();
    if (params.topic) search.set("topic", params.topic);
    if (params.q) search.set("q", params.q);
    return request<Question[]>(`/api/questions${search.size ? `?${search}` : ""}`);
  },
  question: (id: string) => request<Question>(`/api/questions/${id}`),
  startSession: (data: { size: number }) =>
    request<{ questions: Question[] }>("/api/sessions", "POST", data),
  submitAttempt: (data: {
    questionId: string;
    selfRating?: "got" | "missed";
    userAnswer?: string;
    aiScore?: number;
    aiFeedback?: Grade;
  }) => request<{ ok: true }>("/api/attempts", "POST", data),
  progress: () => request<ProgressSummary>("/api/progress/summary"),
  grade: async (data: { questionId: string; userAnswer: string }): Promise<Grade> => {
    const job = await request<{ jobId: string }>("/api/grade", "POST", data);
    const res = await fetch(`/api/grade/${job.jobId}/stream`, {
      credentials: "same-origin",
      cache: "no-store",
    });
    const stream = await res.text();
    if (!res.ok) throw new Error(stream || `Request failed (${res.status})`);

    const event = stream
      .split(/\r?\n\r?\n/)
      .find((block) => /^event:[ \t]*(complete|error)[ \t]*$/m.test(block));
    if (!event) throw new Error("AI grading ended without a result. Please try again.");

    const eventName = event.match(/^event:[ \t]*(complete|error)[ \t]*$/m)?.[1];
    const payload = event
      .split(/\r?\n/)
      .filter((line) => /^data:/.test(line))
      .map((line) => line.replace(/^data:\s?/, ""))
      .join("\n");
    const result = JSON.parse(payload) as Grade | { message?: string };
    if (eventName === "error") {
      throw new Error("message" in result && result.message ? result.message : "AI grading failed");
    }
    return result as Grade;
  },
};
