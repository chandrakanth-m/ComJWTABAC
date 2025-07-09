import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = `${environment.apiUrl}/tasks`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  // Get all tasks (ABAC: Admin and Moderator see all, Users see only their own)
  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(this.apiUrl);
  }

  // Get tasks for current user only
  getMyTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/my`);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  createTask(task: Omit<Task, 'id' | 'owner'>): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task);
  }

  updateTask(id: number, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleTaskStatus(task: Task): Observable<Task> {
    const updatedTask = {
      completed: !task.completed
    };
    return this.updateTask(task.id!, updatedTask);
  }

  // ABAC helper methods
  canEditTask(task: Task): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    // Admin and Moderator can edit any task
    if (this.authService.isAdmin() || this.authService.isModerator()) {
      return true;
    }
    
    // Users can only edit their own tasks
    return task.owner?.id === currentUser.id;
  }

  canDeleteTask(task: Task): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    // Admin can delete any task
    if (this.authService.isAdmin()) {
      return true;
    }
    
    // Users can only delete their own tasks
    return task.owner?.id === currentUser.id;
  }

  canViewAllTasks(): boolean {
    return this.authService.isAdmin() || this.authService.isModerator();
  }
}