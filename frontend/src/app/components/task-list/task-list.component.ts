import { Component, OnInit } from '@angular/core';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  loading = false;
  errorMessage = '';
  currentUser: User | null = null;
  showAllTasks = false;

  constructor(
    private taskService: TaskService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.showAllTasks = this.taskService.canViewAllTasks();
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    const taskObservable = this.showAllTasks ? 
      this.taskService.getAllTasks() : 
      this.taskService.getMyTasks();
      
    taskObservable.subscribe({
      next: (data) => {
        this.tasks = data;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading tasks. Please try again later.';
        console.error('Error loading tasks:', error);
        this.loading = false;
      }
    });
  }

  toggleTaskStatus(task: Task): void {
    if (!this.canEditTask(task)) {
      this.errorMessage = 'You do not have permission to edit this task.';
      return;
    }
    
    this.taskService.toggleTaskStatus(task)
      .subscribe({
        next: (updatedTask) => {
          // Update the task in the list
          const index = this.tasks.findIndex(t => t.id === updatedTask.id);
          if (index !== -1) {
            this.tasks[index] = updatedTask;
          }
          this.errorMessage = '';
        },
        error: (error) => {
          console.error('Error updating task status:', error);
          this.errorMessage = 'Error updating task status. Please try again.';
        }
      });
  }

  deleteTask(id: number): void {
    const task = this.tasks.find(t => t.id === id);
    if (task && !this.canDeleteTask(task)) {
      this.errorMessage = 'You do not have permission to delete this task.';
      return;
    }
    
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id)
        .subscribe({
          next: () => {
            this.tasks = this.tasks.filter(task => task.id !== id);
            this.errorMessage = '';
          },
          error: (error) => {
            console.error('Error deleting task:', error);
            this.errorMessage = 'Error deleting task. Please try again.';
          }
        });
    }
  }

  // ABAC helper methods
  canEditTask(task: Task): boolean {
    return this.taskService.canEditTask(task);
  }

  canDeleteTask(task: Task): boolean {
    return this.taskService.canDeleteTask(task);
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isModerator(): boolean {
    return this.authService.isModerator();
  }

  toggleViewMode(): void {
    this.showAllTasks = !this.showAllTasks;
    this.loadTasks();
  }

  getTaskOwnerDisplay(task: Task): string {
    if (!task.owner) return 'Unknown';
    return task.owner.username;
  }

  isOwnTask(task: Task): boolean {
    return task.owner?.id === this.currentUser?.id;
  }
}