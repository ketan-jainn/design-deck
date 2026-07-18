import type { Category, Grade, Profile, ProgressSummary, Question } from "./types";

type Method = "GET" | "POST" | "PATCH";

async function request<T>(path: string, method: Method = "GET", body?: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    headers: body ? { "content-type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 401 && typeof window !== "undefined" && !location.pathname.startsWith("/auth")) {
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
    request<Profile>("/api/auth/signup", "POST", data),
  login: (data: { email: string; password: string }) =>
    request<Profile>("/api/auth/login", "POST", data),
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
  grade: (data: { questionId: string; userAnswer: string }) => request<Grade>("/api/grade", "POST", data),
};
