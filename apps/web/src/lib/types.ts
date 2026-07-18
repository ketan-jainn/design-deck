export type Category = {
  id: string;
  name: string;
  slug: string;
  color: string;
  sortOrder: number;
};

export type AnswerKey = {
  bullets: string[];
  explanation: string;
  followUps: string[];
  commonMistakes: string[];
  whenNotToUse: string;
};

export type Question = {
  id: string;
  prompt: string;
  qtype: string;
  difficulty: string;
  companies: string[];
  sources: string[];
  category: Pick<Category, "name" | "slug" | "color"> | null;
  answerKey: AnswerKey | null;
};

export type Profile = {
  email: string;
  displayName: string;
  dailyGoal: number;
  streak: number;
};

export type Grade = {
  score: number;
  missing: string[];
  wrong: string[];
  improvements: string[];
  summary: string;
};

export type ProgressSummary = {
  totalAnswered: number;
  accuracy: number;
  streak: number;
  dailyGoal: number;
  todayCount: number;
  dueCount: number;
  weakest: TopicMastery[];
  strongest: TopicMastery[];
};

export type TopicMastery = {
  name: string;
  slug: string;
  color: string;
  mastery: number;
};
