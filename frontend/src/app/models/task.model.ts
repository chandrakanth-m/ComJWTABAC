export interface Task {
  id?: number;
  title: string;
  description: string;
  completed: boolean;
  owner?: {
    id: number;
    username: string;
  };
}